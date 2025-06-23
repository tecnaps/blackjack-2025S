package json;

import utilities.enums.Move;

public class TurnACK {
    public String type = "action";
    public Move move;

    public TurnACK (Move move){
        this.move = move;
    }

}
