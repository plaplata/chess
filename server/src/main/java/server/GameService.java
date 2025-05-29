package server;

import com.google.gson.Gson;
import dataaccess.*;
import service.GameData;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import java.sql.*;
import com.google.gson.Gson;
import chess.ChessGame;
import dataaccess.DataAccessException;

import spark.Request;
import spark.Response;
import java.util.Map;


public class GameService {

    private final GameStorage gameStorage;
    private final AuthStorage authStorage;
    private final Gson gson = Server.gson;

    public String createGame(Request req, Response res) {
        String token = req.headers("Authorization");

        try {
            if (!authStorage.isValidToken(token)) {
                res.status(401);
                return error("unauthorized");
            }

            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String gameName = body.get("gameName");

            if (gameName == null || gameName.trim().isEmpty()) {
                res.status(400);
                return error("bad request");
            }

            String username = authStorage.getUsernameByToken(token);
            int id = gameStorage.createGame(gameName, username);

            res.status(200);
            return gson.toJson(Map.of("gameID", id));
        } catch (DataAccessException e) {
            res.status(500);
            return error("database error: " + e.getMessage());
        }
    }


    public GameService(GameStorage gameStorage, AuthStorage authStorage) {
        this.gameStorage = gameStorage;
        this.authStorage = authStorage;
    }

    public String listGames(Request req, Response res) {
        String token = req.headers("Authorization");
        try {
            if (!authStorage.isValidToken(token)) {
                res.status(401);
                return error("unauthorized");
            }

            List<GameData> games = gameStorage.listGames();
            if (games == null) {
                games = List.of(); // fallback safety
                System.out.println("[WARNING] gameStorage.listGames() returned null; replaced with empty list.");
            }

            res.status(200);
            return gson.toJson(Map.of("games", games));
        } catch (DataAccessException e) {
            res.status(500);
            return error("database error: " + e.getMessage());
        }
    }

    public String joinGame(Request req, Response res) {
        String token = req.headers("Authorization");
        try {
            if (!authStorage.isValidToken(token)) {
                res.status(401);
                return error("unauthorized");
            }

            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            String color = (String) body.get("playerColor");
            Double idRaw = (Double) body.get("gameID");

            if (color == null || idRaw == null) {
                res.status(400);
                return error("bad request");
            }

            color = color.toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                res.status(400);
                return error("bad request");
            }

            int gameID = idRaw.intValue();
            String username = authStorage.getUsernameByToken(token);

            if (username == null) {
                res.status(401);
                return error("unauthorized");
            }

            boolean success = gameStorage.joinGame(gameID, username, color);
            if (!success) {
                res.status(403);
                return error("Error: already taken");
            }

            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(500);
            return error("database error: " + e.getMessage());
        }
    }

    private String error(String message) {
        return gson.toJson(Map.of("message", "Error: " + message));
    }
}
