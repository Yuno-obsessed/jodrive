package sanity.nil.exceptions;

public class StorageException extends RuntimeException {

    public StorageException() {}

    public StorageException(String message) {
        super(message);
    }
}
