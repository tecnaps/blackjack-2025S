package json;

public class SurrenderACK {
    public String type = "surrender";
    public String answer;

    public SurrenderACK (String answer){
        this.answer = answer;
    }
}
