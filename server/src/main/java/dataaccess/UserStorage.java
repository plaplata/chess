package dataaccess;

public interface UserStorage {
    void clear();
    boolean addUser(String username, String password, String email);
    boolean validateCredentials(String username, String password);

}
