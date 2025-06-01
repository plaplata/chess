package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import service.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLGameStorage implements GameStorage {

    @Override
    public int createGame(String gameName, String creatorUsername) throws DataAccessException {
        System.out.println("SQLGameStorage.createGame called");  // LOG: Method was entered

        String sql = "INSERT INTO games (gameName, whiteUsername, blackUsername, gameState) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String gameJson = "{}";  // Avoid cyclic serialization for now

            stmt.setString(1, gameName);
            stmt.setString(2, null);        // whiteUsername
            stmt.setString(3, null);        // blackUsername
            stmt.setString(4, gameJson);    // gameState

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int newID = rs.getInt(1);
                    System.out.println("Game inserted with ID: " + newID);
                    return newID;
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection error during createGame: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("connection")) {
                throw new DataAccessException("failed to get connection");
            }
            throw new DataAccessException("Failed to create game: " + e.getMessage());
        }

        return -1;
    }




    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT gameID, gameName, whiteUsername, blackUsername FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                GameData game = new GameData(
                        rs.getInt("gameID"),
                        rs.getString("gameName"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername")
                );
                games.add(game);
            }

        } catch (SQLException e) {
            System.err.println("Connection error during listGames: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("connection")) {
                throw new DataAccessException("failed to get connection");
            }
            throw new DataAccessException("Failed to list games: " + e.getMessage());
        }

        return games;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, gameName, whiteUsername, blackUsername FROM games WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("gameName"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername")
                    );
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve game: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean joinGame(int gameID, String username, String color) throws DataAccessException {
        String query = "SELECT whiteUsername, blackUsername FROM games WHERE gameID = ?";
        String update = null;

        try (Connection conn = DatabaseManager.getConnection()) {
            // Step 1: Check current team assignments
            try (PreparedStatement selectStmt = conn.prepareStatement(query)) {
                selectStmt.setInt(1, gameID);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ joinGame: Game " + gameID + " not found");
                        throw new DataAccessException("Game not found");
                    }

                    String white = rs.getString("whiteUsername");
                    String black = rs.getString("blackUsername");

                    // 🔍 Debug: show current state
                    System.out.println("🔍 joinGame request: user=" + username + ", color=" + color + ", gameID=" + gameID);
                    System.out.println("    Current: white=" + white + ", black=" + black);

                    if (color.equalsIgnoreCase("WHITE")) {
                        if (white != null) {
                            System.out.println("❌ white already taken");
                            throw new DataAccessException("White team already taken");
                        }
                        update = "UPDATE games SET whiteUsername = ? WHERE gameID = ?";
                    } else if (color.equalsIgnoreCase("BLACK")) {
                        if (black != null) {
                            System.out.println("❌ black already taken");
                            throw new DataAccessException("Black team already taken");
                        }
                        update = "UPDATE games SET blackUsername = ? WHERE gameID = ?";
                    } else {
                        System.out.println("❌ invalid color value: " + color);
                        throw new DataAccessException("Invalid color: must be WHITE or BLACK");
                    }
                }
            }

            // Step 2: Perform update
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                updateStmt.setString(1, username);
                updateStmt.setInt(2, gameID);
                int rowsAffected = updateStmt.executeUpdate();
                System.out.println("✅ joinGame update: rowsAffected = " + rowsAffected);
                return rowsAffected == 1;
            }

        } catch (SQLException e) {
            System.err.println("Connection error during joinGame: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("connection")) {
                throw new DataAccessException("failed to get connection");
            }
            throw new DataAccessException("Failed to join game: " + e.getMessage());
        }
    }


    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error Failed to clear games: " + e.getMessage());
        }
    }
}
