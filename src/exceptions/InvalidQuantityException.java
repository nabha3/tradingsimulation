package exceptions;

// Thrown when a quantity makes no sense: zero, negative, or more than we have.
public class InvalidQuantityException extends Exception {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
