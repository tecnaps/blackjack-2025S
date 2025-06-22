import java.util.*;

public class Croupier{

    private int numberOfDecks=0;
    
    public static void main(String[] args){

        new Thread(()-> System.out.println("Hello")).start();

    }

    private void checkPoints(String cards){

        String[] tokens = cards.split(",");
        for(int i = 0; i< tokens.length; i++)
            switch(tokens[i]){
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                case "10":
                case "J":
                case "D":
                case "K":
                case "A":
            }
    }

    // Anforderung

    private String Hit (String player){

        // This method returns a card from the deck
        return "";
    }

    private void Stand(){

    }

    private void DoubleDown (){

    }

    private void Split (){

    }

    private void Surrender (){

    }

    private void Bet(){

    }

    private void Pay(){

    }

    // Abfrage von Statistiken mit Kartenzähler

    private void stats (){

    }

    private void recommendation(){

    }

}