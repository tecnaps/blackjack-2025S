package json;

public class DrawCard implements BlackJackMessage{
    public String type="receive_card";
    public PlayerCard card;

    public DrawCard(PlayerCard card){
        this.card = card;
    }

    @Override
    public String getType(){
        return type;
    }
}
