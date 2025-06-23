import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.ArrayList;
import java.util.List;

import json.*;
import utilities.enums.Role;
import utilities.exceptions.*;

public class Croupier{

    // UDP stuff
    private static int localport=-1;  
    private static InetAddress localhost;
    private static DatagramSocket socket;
    private static final int BUFFER_SIZE=4096;

    // User stuff
    private static HashMap <InetAddress, String> addressLookup = new HashMap <>();
    private static HashMap <String, InetSocketAddress> nameLookup = new HashMap <>();

    // Croupier stuff
    private static List <String> table;
    private static final int MAX_PLAYER = 4;
    private static final int numberOfDecks=6;
    private static int[][] bets = new int[MAX_PLAYER][2];
    private static InetSocketAddress kartenzaehler;
    


    
    public static void main(String[] args)throws IOException{

        new Thread(()-> System.out.println("Hello")).start();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (localport==-1){

            try{

                System.out.println("\nPlease enter a portnumber for the program to listen at. (valid port numbers: 1023 - 65535)");
                localport = Integer.parseInt(input.readLine());

                if(localport < 0 || 65535 < localport)
                    throw new IOException("\nport is out of range.");
                
                if(!freePort(localport))
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

            Thread receiver = new Thread(() -> { try {
                                            receiver(socket);
                                        } catch (IOException e){

                                            System.out.println("receiver shutdown.");
                                            e.printStackTrace();
                                        
                                        }});

            receiver.start();

            blackJack(socket);

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

        while(true){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            JsonNode root = mapper.readTree(message);
            String type = root.get("type").asText();

            switch(type){
                // bet ACK
                case "bet": 
                    BetACK bet = mapper.treeToValue(root, BetACK.class);
                    break;
                
                // error x
                case "error": 
                    MalformedRequest error = mapper.treeToValue(root, MalformedRequest.class);
                    System.out.println(error.message);
                    break;

                // register REQ x     
                case "register": 
                    try{
                    
                        RegREQ register = mapper.treeToValue(root, RegREQ.class);
                        if(register.role==Role.Spieler)
                            registerPlayer(register, packet.getAddress(), packet.getPort());
                        else
                            registerZaehler(register, packet.getAddress(), packet.getPort());
                    
                    } catch (registerException regException){
                            
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
                    SurrenderACK surrender = mapper.treeToValue(root, SurrenderACK.class );
                    break;
                
                // turn ACK
                case "action": 
                    TurnACK turn = mapper.treeToValue(root, TurnACK.class);
                    break;
                
            }
        }
    }

    private static void blackJack(DatagramSocket socket){

        // The game happens here 

        while(true)
        ;

    }


    private static void registerPlayer (RegREQ register, InetAddress srcAddress, int srcPort) throws registerException{

        // This method registers players

        // check whether table exists
        if(table == null)
            table = new ArrayList <>();
        
        // check whether table is full
        if(table.size() != MAX_PLAYER){

            if(!addressLookup.containsKey(srcAddress)){
                InetSocketAddress contact = new InetSocketAddress(srcAddress, srcPort);
                nameLookup.put(register.name, contact);
                addressLookup.put(srcAddress, register.name);
                // add player to the table
                // add player to the bets
            }
        }
        else{
            throw new registerException ("Der Tisch ist bereits voll."); 
        }
    }

    private static void registerZaehler(RegREQ register, InetAddress srcAddress, int srcPort) throws registerException{

        // This method registers Kartenzaehler

        if(kartenzaehler == null)
            kartenzaehler = new InetSocketAddress(srcAddress, srcPort);
    
        else{
            throw new registerException ("Es ist bereits ein Kartenzähler registriert.");

        }
        }


    // Anforderung

    private String Hit (String player){

        // This method returns a card from the deck
        return "";
    }

    private void Stand(){

    }

    private void DoubleDown (){

    }

    private void Split (){

    }

    private void Surrender (){

    }

    private void Bet(){

    }

    private void Pay(){

    }

    // Abfrage von Statistiken mit Kartenzähler

    private void stats(){

    }

    private void recommendation(){

    }

    // Utility

    private static boolean freePort(int port){

        // This method checks whether a port is available

        try(DatagramSocket socket = new DatagramSocket(port)){
            socket.close();
            return true;
        } catch (IOException e){
            // port is already in use

            System.out.println(port + " is already in use.");

            return false;
        }
    }


}