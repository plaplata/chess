package dataaccess;

public interface AuthStorage {
    void addToken(String authToken, String username) throws DataAccessException;
    boolean isValidToken(String authToken) throws DataAccessException;
    String getUsernameByToken(String authToken) throws DataAccessException;
    void removeToken(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
