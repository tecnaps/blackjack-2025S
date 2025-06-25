import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import json.*;
import utilities.enums.Role;
import utilities.exceptions.*;
import utilities.UtilityCloset;
import utilities.MessageBus;
import utilities.MessageParser;

public class Croupier{

    // UDP stuff
    private static int localport=-1;  
    private static InetAddress localhost;
    private static DatagramSocket socket;
    private static final int BUFFER_SIZE=4096;
    private static BufferedReader input;
    private static MessageBus bus = new MessageBus();


    // Croupier stuff
    private static Table table;
    private static final int MAX_PLAYER = 4;
    private static final int numberOfDecks=6;
    private static Deck deck;
    private static Hand croupierHand;
    
    //private static int[][] bets = new int[MAX_PLAYER][2];
        
    private static HashMap <InetAddress, String> addressLookup = new HashMap <>();

    public static void main(String[] args)throws IOException{

        new Thread(()-> System.out.println("Hello")).start();

        input = new BufferedReader(new InputStreamReader(System.in));

        while (localport==-1){

            try{

                System.out.println("\nPlease enter a portnumber for the program to listen at. (valid port numbers: 1023 - 65535)");
                localport = Integer.parseInt(input.readLine());

                if(localport < 0 || 65535 < localport)
                    throw new IOException("\nport is out of range.");
                
                if(!UtilityCloset.freePort(localport))
                    throw new IOException("\nport is already in use.");
                break;

            } catch (IOException e){
                System.out.println(e.getMessage());
                localport = -1;
            } catch (NumberFormatException e){
                System.out.println("\nPlease input an Integer.");
                localport = -1;
            }

        }
        // get local ip

        try {
        
            localhost = InetAddress.getLocalHost();
        
        }catch(UnknownHostException e){
        
            System.out.println("Unable to determine local IP address.");
        
        }

        try{

            socket = new DatagramSocket(localport);

            Thread receiver = new Thread(() -> {try{                
                                                    receiver(socket);
                                                } catch (IOException e){
                                                    System.out.println("receiver shutdown.");
                                                }});
            receiver.start();
            initGame();

        } catch(IOException e){

            // add meaningful exception handling

        } finally{
            input.close();
            socket.close();
        }
    }

    private static void receiver (DatagramSocket socket)throws IOException{
        
        // This method receives incomming UDP Packages and parses them for known JSON objects 

        byte[] buffer = new byte[BUFFER_SIZE];
        ObjectMapper mapper = new ObjectMapper();
        BlackJackMessage message = null;

        while(true){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String payload = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            
            try{
                message = MessageParser.parse(payload); 
            } catch (UnknownJsonTypeException jsonException){
                System.out.println(jsonException.getMessage());
                continue;
            } catch (Exception e){
                // generic
            }

            switch(message.getType()){
                // bet ACK
                case "bet": 
                    if(message instanceof BetACK response){
                        CompletableFuture<BetACK> future = bus.betRequests.remove(packet.getAddress());
                        if(future != null)
                            future.complete(response);
                    }

                    break;
                
                // error x
                case "error": 
                    MalformedRequest error = (MalformedRequest) message;
                    System.out.println(error.message);
                    break;

                // register REQ x     
                case "register": 
                    try{                    
                        RegREQ register = (RegREQ) message;

                        if(register.role==Role.Spieler)
                            registerPlayer(register, packet.getAddress(), packet.getPort());
                        else
                            registerZaehler(register, packet.getAddress(), packet.getPort());
                    
                    } catch (RegisterException regException){
                            
                        MalformedRequest errorMSG = new MalformedRequest(regException.getMessage());
                            buffer = mapper.writeValueAsBytes(errorMSG);
                            DatagramPacket sendMSG = new DatagramPacket(buffer, buffer.length,packet.getAddress(), packet.getPort());
                            
                            try{
                                socket.send(sendMSG);
                            }catch(IOException IOExcept){
                                // generic
                            }
                    }
                    break;

                // surrender ACK
                case "surrender": 
                    if (message instanceof SurrenderACK response){
                        CompletableFuture<SurrenderACK> future = bus.surrenderRequests.get(packet.getAddress());
                        if(future != null)
                            future.complete(response);
                    }
                    break;
                
                // turn ACK
                case "action": 
                    if (message instanceof TurnACK response){
                        CompletableFuture<TurnACK> future = bus.turnRequests.get(packet.getAddress());
                        if(future != null)
                            future.complete(response);
                    }

                    break;
                
            }
        }
    }

    private static void initGame(){

        // setup game objects
        table = new Table(MAX_PLAYER);
        deck = new Deck(numberOfDecks);
        deck.shuffle();

        // setup input stuff
        String line;
        
        while(true){
            try{
            line = input.readLine();

            // splitting strings
            line = line.trim();
            String[] tokens = line.split("\\s+");

            switch(tokens[0]){
                case "start":
                    if (table.getPlayerCnt() == 0)
                        throw new StartGameException("No player has been registered at this table.");
                    
                    if (table.getKartenzaehler() == null)
                        throw new StartGameException("No kartenzaehler has been registered at this table.");
                    
                    System.out.println("Starting game.");

                    // add message to all registered players and kartenzaehler
                    
                    blackJack();
                    break;
                
                case "exit": return;
                
                default:
                    System.out.println(line + " is not a known command.");
                }
            } catch (StartGameException startException){
                System.out.println(startException.getMessage());
            } catch (IOException e){
            // generic
            }

        }
    }

    private static void blackJack(){

        // This is where the game happens

        byte[] BlackJackBuffer = new byte[BUFFER_SIZE];
        ObjectMapper mapper = new ObjectMapper();
        
        // eject players that have won to often
        // send list of players to kartenzaehler

        // deal cards

        try{
            for(int turn = 0; turn < 2; turn++){
                for (Seat seat: table.getSeats()){
                    if(seat.isOccupied()){
                
                        Player player = seat.getPlayer();
                        InetSocketAddress contact = player.getContact();

                        if (player.getNumberOfHands()==0){
                            
                            // create new Hand
                            Hand newHand = new Hand(player.getNumberOfHands()+1);
                            newHand.setPlayer(player);

                            // draw card
                            Card newCard = deck.draw();

                            // add to hand
                            newHand.addCard(newCard);

                            // add hand
                            player.addHand(newHand);

                            // send card to player
                            DrawCard newCardPrep = new DrawCard(new PlayerCard(newCard.getRank(), newCard.getSuit()));
                            BlackJackBuffer = mapper.writeValueAsBytes(newCardPrep);
                            DatagramPacket newCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                            socket.send(newCardPack);

                            // send new card to kartenzaehler

                        }
                        else {
                            // first round finished, player must have 1 hand

                            // draw card

                            Card newCard = deck.draw();

                            // add to hand

                            player.getHand(1).addCard(newCard);

                            // send new card to player

                            DrawCard newCardPrep = new DrawCard(new PlayerCard(newCard.getRank(), newCard.getSuit()));
                            BlackJackBuffer = mapper.writeValueAsBytes(newCardPrep);
                            DatagramPacket newCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                            socket.send(newCardPack);

                            // send new card to kartenzaehler
                        }
                    }
                }
                // Croupier logic
                if(croupierHand == null){
                    croupierHand = new Hand(1);
                    croupierHand.addCard(deck.draw());

                    // the first croupier card is send to each player with the first TurnREQ below
                    
                    // send new card to kartenzaehler

                } else {
                    croupierHand.addCard(deck.draw());

                    // card is hidden
                }
            }

        } catch(DeckException DeckException){
            System.out.println(DeckException.getMessage());
            
            // return if no cards in Deck b/c not all players have sufficient cards to play
            // possibly add more robust exception handling

            return;
        } catch (Exception e){
            // generic
        }

        // betting phase

        try{
            for(Seat seat: table.getSeats()){
                if(seat.isOccupied()){
                    
                    InetSocketAddress contact = seat.getPlayer().getContact();

                    // send BetREQ

                    BetREQ betREQ = new BetREQ();
                    BlackJackBuffer = mapper.writeValueAsBytes(betREQ);
                    DatagramPacket sendBetREQ = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                    socket.send(sendBetREQ);

                    // create future
                    
                    CompletableFuture<BetACK> future = new CompletableFuture<>();
                    bus.betRequests.put(contact.getAddress(), future);

                    // wait for response

                    BetACK futureResponse = future.get(180, TimeUnit.SECONDS);

                    // place bet

                    seat.getPlayer().getHand(1).setInitialBet(futureResponse.amount);

                }
            }
        } catch (TimeoutException timeoutException){
            // generic
        } catch (Exception e){
            // generic for JSONProcessingException (mapper)
            // generic for IOException (socket)
            // generic for the InterruptedException and the ExecutionException (future)
        }

        // test for blackJack
        
        if (croupierHand.checkBlackJack()){

            // compute payouts and send results

            double multiplier = 0;

            for(Seat seat: table.getSeats()){

                if (seat.isOccupied()){
                    // compute payouts                
                    if (seat.getPlayer().getHand(1).checkBlackJack()){
                        seat.getPlayer().payout(1.5);
                        multiplier = 1.5;
                    } else{
                        seat.getPlayer().payout(-1);
                        multiplier = -1;
                    }
                    
                    // get contact details
                    InetSocketAddress contact = seat.getPlayer().getContact();

                    // send results
                    try{
                        Result resultPrep = new Result((int) (seat.getPlayer().getBet()*multiplier), multiplier==1.5? "You won.": "You lost.");
                        BlackJackBuffer = mapper.writeValueAsBytes(resultPrep);
                        DatagramPacket resultPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                        socket.send(resultPack);
                    } catch (IOException e){
                        // generic
                    }
                }
            }
            
            // cleanup hands and bets
            for(Seat seat: table.getSeats())
                if(seat.isOccupied())
                    seat.getPlayer().cleanup();

            // game over
            return;
        }

        // ask for surrender

        try{
            int surrenderCnt = 0;

            for(Seat seat: table.getSeats()){
                if(seat.isOccupied()){
                    // get contact details
                    InetSocketAddress contact = seat.getPlayer().getContact();

                    // send surrender request
                    SurrenderREQ surrenderRequest = new SurrenderREQ();
                    BlackJackBuffer = mapper.writeValueAsBytes(surrenderRequest);
                    DatagramPacket sendRequest = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                    socket.send(sendRequest);
                
                    // create future
                    CompletableFuture<SurrenderACK> future = new CompletableFuture<>();
                    bus.surrenderRequests.put(contact.getAddress(), future); 

                    // wait for response
                    SurrenderACK futureResponse = future.get(180, TimeUnit.SECONDS);

                    // add logic to interprete response/
                    if(futureResponse.answer.equalsIgnoreCase("yes")){
                        
                        seat.getPlayer().surrender();
                        surrenderCnt++;

                        // calculate payout
                        seat.getPlayer().payout(0.5);

                        // send result
                        Result resultPrep = new Result((int) (seat.getPlayer().getBet()*0.5), "You surrendered.");
                        BlackJackBuffer = mapper.writeValueAsBytes(resultPrep);
                        DatagramPacket resultPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                        socket.send(resultPack);
                    }
                }
            }

            if(surrenderCnt == table.getPlayerCnt()){

                //return to initial state
                for(Seat seat: table.getSeats())
                    if(seat.isOccupied())
                        seat.getPlayer().cleanup();

                // game over
                return;
            }

        } catch (Exception e){
            // generic
        }

        // player turns

        for(Seat seat: table.getSeats()){
            
            if(seat.isOccupied()){

                try{

                    InetSocketAddress contact = seat.getPlayer().getContact();
                    
                    // send turn request

                    PlayerCard croupierCard = new PlayerCard(croupierHand.getCard(1).getRank(), croupierHand.getCard(1).getSuit()); 
                    TurnREQ croupierCardPrep = new TurnREQ(croupierCard);
                    BlackJackBuffer = mapper.writeValueAsBytes(croupierCardPrep);
                    DatagramPacket croupierCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                    socket.send(croupierCardPack);
            
                    do{

                        // create future

                        CompletableFuture<TurnACK> future = new CompletableFuture<>();
                        bus.turnRequests.put(contact.getAddress(), future);

                        // auf antwort warten
                
                        TurnACK futureResponse = future.get(180, TimeUnit.SECONDS);

                        switch(futureResponse.move){
                            case "hit": 
                                
                                // draw card

                                Card newCard = deck.draw();
                                seat.getPlayer().getHand(futureResponse.handIndex).addCard(newCard);

                                // send card

                                DrawCard newCardPrep = new DrawCard(new PlayerCard(newCard.getRank(), newCard.getSuit()));
                                BlackJackBuffer = mapper.writeValueAsBytes(newCardPrep);
                                DatagramPacket newCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(),contact.getPort());
                                socket.send(newCardPack);

                                break;

                            case "split": 

                                //split hand

                                Hand secondHand = seat.getPlayer().getHand(futureResponse.handIndex).split();
                                Hand firstHand = seat.getPlayer().getHand(futureResponse.handIndex);
                                secondHand.setPlayer(seat.getPlayer());
                                seat.getPlayer().addHand(secondHand);

                                // request additional bet (equal to original)
                                
                                secondHand.setInitialBet(firstHand.getInitialBet());

                                // draw card for first hand

                                Card firstCard = deck.draw();
                                firstHand.addCard(firstCard);

                                // send card

                                DrawCard firstCardPrep = new DrawCard(new PlayerCard(firstCard.getRank(), firstCard.getSuit()));
                                BlackJackBuffer = mapper.writeValueAsBytes(firstCardPrep);
                                DatagramPacket firstCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                                socket.send(firstCardPack);

                                // draw card for second hand

                                Card secondCard = deck.draw();
                                secondHand.addCard(secondCard);

                                // send card

                                DrawCard secondCardPrep = new DrawCard(new PlayerCard(secondCard.getRank(), secondCard.getSuit()));
                                BlackJackBuffer = mapper.writeValueAsBytes(secondCardPrep);
                                DatagramPacket secondCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                                socket.send(secondCardPack);

                                break;

                            case "double_down": 

                                // double original bet
                                
                                seat.getPlayer().getHand(futureResponse.handIndex).doubleDown();

                                // get 1 more card

                                newCard = deck.draw();
                                seat.getPlayer().getHand(futureResponse.handIndex).addCard(newCard);

                                // send card

                                newCardPrep = new DrawCard(new PlayerCard(newCard.getRank(), newCard.getSuit()));
                                BlackJackBuffer = mapper.writeValueAsBytes(newCardPrep);
                                newCardPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                                socket.send(newCardPack);

                                // end turn

                                seat.getPlayer().getHand(futureResponse.handIndex).stand();

                                break;

                            case "stand": 
                                seat.getPlayer().getHand(futureResponse.handIndex).stand();
                        }

                        // check whether player has any more moves left

                        if(seat.getPlayer().isStandingDown())
                            break;

                } while (!seat.getPlayer().isStandingDown());

                } catch(IOException iOException){
                    // generic
                } catch(Exception e){
                    // generic
                }
            }
        }

        // payout and cleanup

        for(Seat seat: table.getSeats()){
            double earnings = 0;
            if(seat.isOccupied()){
                for(Hand hand: seat.getPlayer().getHands()){
                    if(hand.checkBlackJack())
                        earnings = hand.getInitialBet()*1.5;
                    else if (hand.calculatePoints()<=21 && croupierHand.calculatePoints() < hand.calculatePoints())
                        earnings += hand.getInitialBet();
                    else if (21 < hand.calculatePoints() || hand.calculatePoints() < croupierHand.calculatePoints())
                        earnings -= hand.getInitialBet();
                    }
            }
            seat.getPlayer().payout(earnings);

            // get contact
            InetSocketAddress contact = seat.getPlayer().getContact();

            // send results
            try{
                Result resultPrep = new Result((int) earnings, "Your total earnings are " + (int) earnings);
                BlackJackBuffer = mapper.writeValueAsBytes(resultPrep);
                DatagramPacket resultPack = new DatagramPacket(BlackJackBuffer, BUFFER_SIZE, contact.getAddress(), contact.getPort());
                socket.send(resultPack);
            } catch(IOException iOException){
                // generic
            }

            // cleanup
            seat.getPlayer().cleanup();
        }
    }




    private static void registerPlayer (RegREQ register, InetAddress srcAddress, int srcPort) throws RegisterException{

        // This method registers players

        // check whether table exists
        if(table == null)
            table = new Table(MAX_PLAYER);
        
        // check whether table is full
        if(table.getPlayerCnt() != table.getNumberOfSeats()){

            if(!addressLookup.containsKey(srcAddress)){

                InetSocketAddress contact = new InetSocketAddress(srcAddress, srcPort);
                table.addPlayer(register.name, register.credit, contact);
                addressLookup.put(srcAddress, register.name);
            }
        }
        else{
            throw new RegisterException ("Player is already registered at this table."); 
        }
    }

    private static void registerZaehler(RegREQ register, InetAddress srcAddress, int srcPort) throws RegisterException{

        // This method registers Kartenzaehler

        if(table.getKartenzaehler()==null)
            table.setKartenzaehler(new InetSocketAddress(srcAddress, srcPort));
    
        else{
            throw new RegisterException ("Another kartenzaehler is already registered at this table.");

        }
    }

    // Abfrage von Statistiken mit Kartenzähler

    private void stats(){

    }

    private void recommendation(){

    }
}