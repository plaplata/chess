package server;

import static spark.Spark.*;

import chess.ChessGame;
import com.google.gson.GsonBuilder;
import dataaccess.*;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.Map;

public class Server {

    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ChessGame.class, new ChessGameAdapter())
            .create();

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567);
    }

    public int run(int desiredPort) {
        boolean useMemory = false;
        System.out.println("Server using " + (useMemory ? "Memory" : "SQL") + " storage backend.");

        port(desiredPort);
        staticFiles.location("/web");

        UserStorage users = useMemory ? new UserMemoryStorage() : new SQLUserStorage();
        try {
            DatabaseManager.createTablesIfNotExists();
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database tables: " + e.getMessage());
        }

        AuthStorage auths = useMemory ? new AuthMemoryStorage() : new SQLAuthStorage();


        GameStorage games = useMemory ? new GameMemoryStorage() : new SQLGameStorage();

        // Register services
        UserReg userReg = new UserReg(users, auths);
        UserLogin userLogin = new UserLogin(users, auths);
        UserLogout userLogout = new UserLogout(auths);
        ClearService clearService = new ClearService(users, auths, games);
        GameService gameService = new GameService(games, auths);

        // Global auth filter (only apply to protected routes)
        before((request, response) -> {
            String path = request.pathInfo();
            String method = request.requestMethod();

            boolean requiresAuth =
                    !(path.equals("/user") && method.equals("POST")) &&
                            !(path.equals("/session") && method.equals("POST")) &&
                            !(path.equals("/session") && method.equals("DELETE")) &&
                            !(path.equals("/db") && method.equals("DELETE"));

            if (requiresAuth) {
                String authToken = request.headers("Authorization");
                if (authToken == null || auths.getToken(authToken) == null) {
                    response.status(401);
                    response.type("application/json");
                    halt(401, new Gson().toJson(Collections.singletonMap("message", "Error: unauthorized")));
                }
            }
        });

        // Register routes
        post("/user", userReg::register);
        post("/session", userLogin::login);
        delete("/session", userLogout::logout);
        delete("/db", (req, res) -> clearService.clearAll(req, res));

        // Game-related routes
        post("/game", (req, res) -> {
            try {
                return gameService.createGame(req, res);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: unexpected failure"));
            }
        });

        get("/game", (req, res) -> {
            try {
                return gameService.listGames(req, res);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: unexpected failure"));
            }
        });

        put("/game", (req, res) -> {
            try {
                return gameService.joinGame(req, res);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: unexpected failure"));
            }
        });



        // Error handler
        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(Map.of("message", "Error: unexpected failure")));
        });

        init();
        awaitInitialization();

        return port();
    }

    public void stop() {
        spark.Spark.stop();
        spark.Spark.awaitStop();
    }
}
