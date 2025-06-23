package json;

public class TurnREQ{
    public String type = "your_turn";
    public Card card;

    public TurnREQ(Card card){
        this.card = card;
    }
}