package exceptions;

// Thrown at login when no user has the given id.
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
