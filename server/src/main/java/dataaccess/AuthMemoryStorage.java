package dataaccess;

import model.AuthToken;

import java.util.HashMap;
import java.util.Map;

public class AuthMemoryStorage implements AuthStorage {

    private final Map<String, String> tokens = new HashMap<>();

    @Override
    public void insertToken(AuthToken token) throws DataAccessException {
        tokens.put(token.getAuthToken(), token.getUsername());
    }

    @Override
    public String addToken(String username) throws DataAccessException {
        String token = java.util.UUID.randomUUID().toString();
        tokens.put(token, username);
        System.out.println("Generated token: " + token + " for user: " + username);
        return token;
    }

    @Override
    public AuthToken getToken(String token) throws DataAccessException {
        if (tokens.containsKey(token)) {
            return new AuthToken(token, tokens.get(token));
        }
        return null;
    }

    @Override
    public void deleteToken(String token) throws DataAccessException {
        tokens.remove(token);
    }

    @Override
    public void clear() throws DataAccessException {
        tokens.clear();
    }
}
