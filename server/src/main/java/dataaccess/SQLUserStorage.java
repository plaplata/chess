package dataaccess;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserStorage implements UserStorage {

    private boolean lastErrorWasConnectionIssue = false;
    public boolean wasConnectionError() {
        return lastErrorWasConnectionIssue;
    }

    @Override
    public boolean addUser(String username, String password, String email) throws DataAccessException{
        lastErrorWasConnectionIssue = false;
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
            String msg = e.getMessage().toLowerCase().trim();
            //System.err.println("SQL EXCEPTION: [" + msg + "]");

            // ✅ Handle expected duplication scenario
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("primary")) {
                return false;  // triggers 403
            }

            if (msg.contains("connection")) {
                lastErrorWasConnectionIssue = true;
            }

            throw new DataAccessException("Failed to add user: " + e.getMessage());
        }
    }

    @Override
    public boolean validateCredentials(String username, String password) throws DataAccessException {
        String sql = "SELECT passwordHash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("passwordHash");
                    return BCrypt.checkpw(password, storedHash);
                }
                return false;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error Failed to validate credentials: " + e.getMessage(), e);
        }
    }



    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error Failed to clear users: " + e.getMessage(), e);
        }
    }

}
