package json;

public class TurnREQ implements BlackJackMessage{
    public String type = "your_turn";
    public PlayerCard card;

    public TurnREQ(PlayerCard card){
        this.card = card;
    }

    @Override
    public String getType(){
        return type;
    }
}