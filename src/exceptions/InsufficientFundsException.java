package exceptions;

// Thrown when a trader tries to buy more than their wallet can pay for.
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
