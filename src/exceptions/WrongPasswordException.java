package exceptions;

// Thrown at login when the user exists but the password is wrong.
public class WrongPasswordException extends Exception {
    public WrongPasswordException(String message) {
        super(message);
    }
}
