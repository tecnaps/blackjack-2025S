package json;

public class DrawCard {
    public String type="receive_card";
    public Card card;

    public DrawCard(Card card){
        this.card = card;
    }
}
