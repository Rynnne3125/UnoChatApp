package coreapp.util.session;

import model.User;
import java.io.*;

/**
 * Manages the current user session by saving and loading user data.
 */
public class CurrentUserManager {
    private static CurrentUserManager instance;
    private User currentUser;

    private CurrentUserManager() {}

    public static synchronized CurrentUserManager getInstance() {
        if (instance == null) {
            instance = new CurrentUserManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
