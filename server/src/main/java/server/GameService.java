package server;

import com.google.gson.Gson;
import dataaccess.AuthStorage;
import dataaccess.DataAccessException;
import dataaccess.GameStorage;
import service.GameData;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

public class GameService {

    private final GameStorage gameStorage;
    private final AuthStorage authStorage;
    private final Gson gson = new Gson();

    public GameService(GameStorage gameStorage, AuthStorage authStorage) {
        this.gameStorage = gameStorage;
        this.authStorage = authStorage;
    }

    public String createGame(Request req, Response res) {
        try {
            String token = req.headers("Authorization");

            if (!authStorage.isValidToken(token)) {
                res.status(401);
                return error("unauthorized");
            }

            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String gameName = body.get("gameName");

            if (gameName == null || gameName.isEmpty()) {
                res.status(400);
                return error("bad request");
            }

            // 🔐 May throw if DB is unavailable
            String username = authStorage.getUsernameByToken(token);

            int id = gameStorage.createGame(gameName, username);

            res.status(200);
            return gson.toJson(Map.of("gameID", id));

        } catch (DataAccessException e) {
            res.status(500);
            return error("Error: " + e.getMessage());
        }
    }




    public String listGames(Request req, Response res) {
        try {
            String token = req.headers("Authorization");
            if (!authStorage.isValidToken(token)) {
                res.status(401);
                return error("unauthorized");
            }

            List<GameData> games = gameStorage.listGames();
            res.status(200);
            return gson.toJson(Map.of("games", games));

        } catch (DataAccessException e) {
            res.status(500);
            return error("Error: " + e.getMessage());
        }
    }


    public String joinGame(Request req, Response res) {
        try {
            String token = req.headers("Authorization");
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

            // Normalize and validate color
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

            boolean joined = gameStorage.joinGame(gameID, username, color);
            if (!joined) {
                res.status(403);
                return error("already taken or invalid");
            }

            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            if (e.getMessage().toLowerCase().contains("team already taken")) {
                res.status(403);
                return error("team already taken");
            }

            res.status(500);
            return error("Error: " + e.getMessage());
        }
    }


    private String error(String message) {
        return gson.toJson(Map.of("message", "Error: " + message));
    }
}
