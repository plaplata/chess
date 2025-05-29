package dataaccess;

public interface UserStorage {
    void clear() throws DataAccessException;
    boolean addUser(String username, String password, String email) throws DataAccessException;
    boolean validateCredentials(String username, String password) throws DataAccessException;

}
