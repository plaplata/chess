package dataaccess;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserStorage implements UserStorage {

    @Override
    public boolean addUser(String username, String password, String email) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, passwordHash, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.executeUpdate();
            return true;

        } catch (SQLException | DataAccessException e) {
            System.err.println("Failed to add user: " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean validateCredentials(String username, String password) {
        String sql = "SELECT passwordHash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("passwordHash");
                return BCrypt.checkpw(password, storedHash);
            }

        } catch (SQLException | DataAccessException e) {
            System.err.println("Failed to validate user: " + e.getMessage());
        }

        return false;
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            System.err.println("Failed to clear users: " + e.getMessage());
        }
    }

}
