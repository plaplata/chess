package dataaccess;

import java.util.HashMap;
import java.util.Map;

public class AuthMemoryStorage implements AuthStorage {

    private final Map<String, String> tokens = new HashMap<>();

    @Override
    public void addToken(String authToken, String username) {
        tokens.put(authToken, username);
    }

    @Override
    public boolean isValidToken(String authToken) {
        return tokens.containsKey(authToken);
    }

    @Override
    public String getUsernameByToken(String authToken) {
        return tokens.get(authToken);
    }
    @Override
    public void removeToken(String authToken) {
        tokens.remove(authToken);
    }

    @Override
    public void clear() {
        tokens.clear();
    }
}
