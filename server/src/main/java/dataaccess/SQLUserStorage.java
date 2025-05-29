package dataaccess;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserStorage implements UserStorage {

    @Override
    public boolean addUser(String username, String password, String email) throws DataAccessException{
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, passwordHash, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            // Check if the error is due to duplicate username (SQLState 23000 or MySQL error code 1062)
            if (e.getSQLState().startsWith("23")) {
                return false;  // user already exists
            }

            // Otherwise, it's a real failure
            throw new DataAccessException("Failed to add user", e);
        }
    }



    @Override
    public boolean validateCredentials(String username, String password) throws DataAccessException {
        String sql = "SELECT passwordHash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("passwordHash");
                return BCrypt.checkpw(password, storedHash);
            }

            return false;  // Username not found

        } catch (SQLException e) {
            System.err.println("Failed to validate user: " + e.getMessage());
            throw new DataAccessException("Failed to validate user", e);
        }
    }


    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DEBUG][SQLUserStorage] Failed to clear users: " + e.getMessage());
            throw new DataAccessException("Error clearing user table", e);
        }
    }

}
