package dataaccess;

import model.AuthToken;

public interface AuthStorage {
    void insertToken(AuthToken token) throws DataAccessException;

    String addToken(String username) throws DataAccessException;

    AuthToken getToken(String token) throws DataAccessException;

    void deleteToken(String token) throws DataAccessException;

    void clear() throws DataAccessException;

    default boolean isValidToken(String token) throws DataAccessException {
        return getToken(token) != null;
    }

    default String getUsernameByToken(String token) throws DataAccessException {
        AuthToken t = getToken(token);
        return t != null ? t.getUsername() : null;
    }
}
