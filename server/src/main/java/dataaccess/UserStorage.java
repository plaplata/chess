package dataaccess;

public interface UserStorage {
    void clear();
    boolean addUser(String username, String password, String email) throws DataAccessException;
    boolean validateCredentials(String username, String password) throws DataAccessException;

}
