package server;

import static spark.Spark.*;

import dataaccess.*;
import com.google.gson.Gson;

import java.util.Collections;

public class Server {

    public static Gson gson = new Gson();

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567, true); // WebSocket enabled in CLI launch
    }

    // ✅ Updated method signature to accept WebSocket toggle
    public int run(int desiredPort, boolean enableWebSockets) {
        port(desiredPort);
        staticFiles.location("/web");

        UserStorage users = new SQLUserStorage();
        try {
            DatabaseManager.createTablesIfNotExists();
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database tables: " + e.getMessage());
        }
        AuthStorage auths = new SQLAuthStorage();
        GameStorage games = new SQLGameStorage();

        UserReg userReg = new UserReg(users, auths);
        UserLogin userLogin = new UserLogin(users, auths);
        UserLogout userLogout = new UserLogout(auths);
        ClearService clearService = new ClearService(users, auths, games);
        GameService gameService = new GameService(games, auths);

        // ✅ Conditionally register WebSocket
        if (enableWebSockets) {
            webSocket("/connect", server.websocket.WebSocketHandler.class);
        }

        // Global auth filter
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
                if (authToken == null || !auths.isValidToken(authToken)) {
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

        post("/game", gameService::createGame);
        get("/game", gameService::listGames);
        put("/game", gameService::joinGame);

        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body("{\"message\": \"Error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        });

        init();
        awaitInitialization();

        return port();
    }

    // Default run for test compatibility
    public int run(int desiredPort) {
        return run(desiredPort, false); // WebSocket disabled by default
    }

    public void stop() {
        spark.Spark.stop();
        spark.Spark.awaitStop();
    }
}
