package dataaccess;

import model.AuthToken;

public interface AuthStorage {
    void insertToken(AuthToken token) throws DataAccessException;

    // ✅ Only one addToken method allowed!
    String addToken(String username) throws DataAccessException;

    AuthToken getToken(String token) throws DataAccessException;

    void deleteToken(String token) throws DataAccessException;

    void clear() throws DataAccessException;

    // ✅ Default method for token validation
    default boolean isValidToken(String token) throws DataAccessException {
        return getToken(token) != null;
    }

    // ✅ Default method for username retrieval
    default String getUsernameByToken(String token) throws DataAccessException {
        AuthToken t = getToken(token);
        return t != null ? t.getUsername() : null;
    }
}
