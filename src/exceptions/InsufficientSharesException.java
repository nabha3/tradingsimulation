package exceptions;

// Thrown when a trader tries to sell more shares than they own.
public class InsufficientSharesException extends Exception {
    public InsufficientSharesException(String message) {
        super(message);
    }
}
