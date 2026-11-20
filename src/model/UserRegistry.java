package model;

import exceptions.UserNotFoundException;
import exceptions.WrongPasswordException;
import java.util.ArrayList;

/**
 * Keeps every registered user and handles the login check.
 */
public class UserRegistry {
    private ArrayList<User> users;

    public UserRegistry() {
        this.users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Returns the user whose id and password match.
     * Throws UserNotFoundException if the id is unknown,
     * or WrongPasswordException if the password does not match.
     */
    public User login(String id, String password)
            throws UserNotFoundException, WrongPasswordException {
        for (User user : users) {
            if (user.getId().equals(id)) {
                if (!user.checkPassword(password)) {
                    throw new WrongPasswordException("Wrong password for " + id);
                }
                return user;
            }
        }
        throw new UserNotFoundException("No user found with id " + id);
    }
}
