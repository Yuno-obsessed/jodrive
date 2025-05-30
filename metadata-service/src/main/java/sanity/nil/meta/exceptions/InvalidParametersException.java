package sanity.nil.meta.exceptions;

public class InvalidParametersException extends RuntimeException {

    public InvalidParametersException() {
        super("Invalid parameters");
    }

    public InvalidParametersException(String message) {
        super(message);
    }
}
