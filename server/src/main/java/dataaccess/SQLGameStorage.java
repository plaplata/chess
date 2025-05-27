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
        String sql = "INSERT INTO games (gameName, whiteUsername, gameState) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ChessGame newGame = new ChessGame();
            newGame.getBoard().resetBoard();
            String gameJson = new Gson().toJson(newGame);

            stmt.setString(1, gameName);
            stmt.setString(2, creatorUsername);
            stmt.setString(3, gameJson);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
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
        String column = color.equalsIgnoreCase("WHITE") ? "whiteUsername" : "blackUsername";
        String sql = "UPDATE games SET " + column + " = ? WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, gameID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected == 1;

        } catch (SQLException e) {
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
            throw new DataAccessException("Failed to clear games: " + e.getMessage());
        }
    }
}
