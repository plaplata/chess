package dataAccess;

public interface AuthStorage {
    void addToken(String authToken, String username);
    boolean isValidToken(String authToken);
    String getUsernameByToken(String authToken);
    void removeToken(String authToken);
    void clear();
}
