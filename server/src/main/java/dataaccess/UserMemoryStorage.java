package dataaccess;
import java.util.*;

import dataaccess.UserStorage;

public class UserMemoryStorage implements UserStorage {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public boolean addUser(String username, String password, String email) {
        if (users.containsKey(username) || users.values().stream().anyMatch(u -> u.email.equals(email))) {
            return false;
        }
        users.put(username, new User(username, password, email));
        return true;
    }

    @Override
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    @Override
    public String getPassword(String username) {
        User user = users.get(username);
        return user != null ? user.password : null;
    }

    private static class User {
        @SuppressWarnings("unused")
        String username;
        String password;
        String email;

        User(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
    }
}