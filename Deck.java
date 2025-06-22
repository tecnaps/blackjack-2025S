import java.util.Collections;
import java.util.ArrayList;
import java.util.List;


public class Deck {

    // This class implements a simple multi card deck

    private final List <Card> cards;

    private static final String [] SUITS = {"heart", "diamond", "club", "spade"};
    private static final String [] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

    public Deck(int numberOfDecks){
        cards = new ArrayList<>(52 * numberOfDecks);
    
        for(int i = 0; i < numberOfDecks; i++)
            for(String suit: SUITS)
                for(String rank: RANKS)
                    cards.add(new Card(suit, rank));
    }


    public void shuffle(){
        Collections.shuffle(cards);
    }

    public Card draw(){
        if(cards.isEmpty())
            throw new IllegalStateException("Deck is empty.");
        
        return cards.remove(cards.size()-1);
    }

    public int size(){
        return cards.size();
    }

    public boolean isEmpty(){
        return cards.isEmpty();
    }

}
