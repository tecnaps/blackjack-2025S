import java.util.ArrayList;
import java.util.List;

public class Player {

    private String name;
    private int credit;
    private List <Hand> hands;
    private boolean stand = false;
    private boolean lost = false; 
    private int numberOfHands = 0;
    // InetSocketAddress for Address and port??
    //private int original_bet;
    

    public Player (String name, int credit){

        this.name = name;
        this.credit = credit;
        hands = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public int getCredit(){
        return credit;
    }

    public int getNumberOfHands(){
        return numberOfHands;
    }

    public void lost(){
        lost = true;
    }

    public boolean hasLost(){
        return lost;
    }


    public boolean isStandingDown(){
        
        for(Hand hand: hands)
            if(!hand.isStandingDown()){
                stand = false;
                break;}
        return stand;
    }
    
    public void addHand(Hand hand){
        hands.add(hand);
        numberOfHands++;
    }

    public List<Hand> getHands(){
        return hands;
    }

}
