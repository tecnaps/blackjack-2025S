import java.util.ArrayList;
import java.util.List;

public class Hand {

    private final List<Card> cards;

    public Hand(){
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card){
        cards.add(card);
    }

    public List<Card> getCards(){
        return cards;
    }

    public int size(){
        return cards.size();
    }

    public void clear(){
        cards.clear();
    }

    @Override
    public String toString(){
        if (cards.isEmpty()){
            return "Empty hand";
        }
        StringBuilder sb = new StringBuilder();
        for (Card card: cards)
            sb.append(card.toString()).append(" ");
        return sb.toString();    
    }   
}
