package utilities.exceptions;

public class registerException extends Exception {
    public registerException(String message){
        super(message);
    }

    public registerException(String message, Throwable cause){
        super(message, cause);
    }
}
