package json;

public class MalformedRequest {

    public String type = "error";
    public String message;

    public MalformedRequest (String message){
        this.message = message;
    }
    
}
