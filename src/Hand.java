import java.util.ArrayList;
import java.util.List;

public class Hand {

    // This class implements a hand in a card game

    private final List<Card> cards;
    private boolean stand = false;
    private int index;

    public Hand(int index){
        this.cards = new ArrayList<>();
        this.index = index;
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

    public int getHandNr (){
        return index;
    }

    public void clear(){
        cards.clear();
    }

    public void stand(){
        stand = true;
    }

    public boolean isStandingDown (){
        return stand;
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
