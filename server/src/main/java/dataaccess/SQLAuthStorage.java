package dataaccess;

import model.AuthToken;

import java.sql.*;

public class SQLAuthStorage implements AuthStorage {

    @Override
    public void insertToken(AuthToken token) throws DataAccessException {
        String sql = "INSERT INTO auth (token, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("[DEBUG] Inserting token for user: " + token.getUsername());

            stmt.setString(1, token.getAuthToken());
            stmt.setString(2, token.getUsername());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting auth token", e);
        }
    }


    @Override
    public String addToken(String username) throws DataAccessException {
        String token = java.util.UUID.randomUUID().toString();
        insertToken(new AuthToken(token, username));
        System.out.println("Generated token: " + token + " for user: " + username);
        return token;
    }

    @Override
    public AuthToken getToken(String token) throws DataAccessException {
        String sql = "SELECT token, username FROM Auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthToken(rs.getString("token"), rs.getString("username"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth token", e);
        }
    }

    @Override
    public void deleteToken(String token) throws DataAccessException {
        String sql = "DELETE FROM Auth WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DEBUG][SQLAuthStorage] Failed to clear auth: " + e.getMessage());
            throw new DataAccessException("Error clearing auth table", e);
        }
    }

}
