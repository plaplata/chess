package dataaccess;

public interface UserStorage {
    void clear();
    boolean addUser(String username, String password, String email);
}
