package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import service.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLGameStorage implements GameStorage {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ChessGame.class, new ChessGameAdapter())
            .create();

    private String serializeGame(ChessGame game) {
        return gson.toJson(game);
    }

    private ChessGame deserializeGame(String json) {
        return gson.fromJson(json, ChessGame.class);
    }

    @Override
    public int createGame(String gameName, String creatorUsername) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, whiteUsername, blackUsername, gameState) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, gameName);
            stmt.setString(2, null);
            stmt.setString(3, null);
            //stmt.setString(4, new Gson().toJson(new ChessGame()));
            stmt.setString(4, "{}"); // Placeholder serialized state

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                else throw new SQLException("No ID returned.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
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
            System.out.println("[ERROR] Failed to list games: " + e.getMessage());
            throw new DataAccessException("Failed to list games", e);  // <-- this change
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
        String column = color.equalsIgnoreCase("WHITE") ? "whiteUsername" : "blackUsername";

        String checkSql = "SELECT " + column + ", gameState FROM games WHERE gameID = ?";
        String updateSql = "UPDATE games SET " + column + " = ?, gameState = ? WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            ChessGame game = null;
            String currentPlayer = null;

            // Step 1: Check if spot is already taken + load game state
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, gameID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        currentPlayer = rs.getString(column);
                        game = deserializeGame(rs.getString("gameState"));
                    }
                }
            }

            if (game == null) {
                return false; // No such game
            }

            // Step 1.5: Check if user is already assigned to opposite color
            String oppositeColumn = color.equalsIgnoreCase("WHITE") ? "blackUsername" : "whiteUsername";
            String checkOppositeSql = "SELECT " + oppositeColumn + " FROM games WHERE gameID = ?";
            String oppositePlayer = null;

            try (PreparedStatement oppStmt = conn.prepareStatement(checkOppositeSql)) {
                oppStmt.setInt(1, gameID);
                try (ResultSet oppRs = oppStmt.executeQuery()) {
                    if (oppRs.next()) {
                        oppositePlayer = oppRs.getString(oppositeColumn);
                    }
                }
            }

            if (currentPlayer != null) {
                return false; // Team already taken
            }


            // Step 2: Save updated player and game state
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, username);
                updateStmt.setString(2, serializeGame(game)); // Preserve original game state
                updateStmt.setInt(3, gameID);
                return updateStmt.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to join game", e);
        }
    }


    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games: " + e.getMessage());
        }
    }
}
