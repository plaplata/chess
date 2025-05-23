package dataaccess;

import java.util.HashMap;
import java.util.Map;

public class UserMemoryStorage implements UserStorage {

    private final Map<String, User> users = new HashMap<>();

    private static class User {
        String password;
        String email;

        User(String password, String email) {
            this.password = password;
            this.email = email;
        }
    }

    @Override
    public boolean addUser(String username, String password, String email) {
        if (users.containsKey(username)){
            return false;
        }
        users.put(username, new User(password, email));
        return true;
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        if (!users.containsKey(username)){
            return false;
        }
        return users.get(username).password.equals(password);
    }

    @Override
    public void clear() {
        users.clear();
    }
}
