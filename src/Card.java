public class Card {

    // This class implements a playing card

    private final String suit;
    private final String rank;
    
    public Card(String suit, String rank){
        this.suit = suit;
        this.rank = rank;
    }
    
    public String getSuit(){
        return suit;
    }

    public String getRank(){
        return rank;
    }

    @Override
    public String toString(){
        return rank + " of " + suit;
    }
}
