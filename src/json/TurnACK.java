package json;

//import utilities.enums.Move;

public class TurnACK implements BlackJackMessage{
    public String type = "action";
    public String move;
    public int handIndex;

    public TurnACK (String move, int handIndex){
        this.move = move;
        this.handIndex = handIndex;
    }

    @Override
    public String getType(){
        return type;
    }
}
