package server;

import static spark.Spark.*;
import com.google.gson.Gson;
import dataaccess.UserMemoryStorage;

public class Server {

    public static Gson gson = new Gson();

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567);
    }

    public int run(int desiredPort) {
        port(desiredPort);
        staticFiles.location("web");

//        // Shared in-memory storage (DAOs)
//        UserMemoryStorage users = new UserMemoryStorage();
//        AuthMemoryStorage auths = new AuthMemoryStorage();
//        GameMemoryStorage games = new GameMemoryStorage();
//
//        // Route handlers
//        UserReg userReg = new UserReg(users, auths);       // Add auth storage
//        UserLogin userLogin = new UserLogin(users, auths);
//        UserLogout userLogout = new UserLogout(auths);
//        GameService gameService = new GameService(games, auths, users);
//        ClearService clearService = new ClearService(users, auths, games);
//
//        // Routes
//        post("/user", userReg::register);
//        post("/session", userLogin::login);
//        delete("/session", userLogout::logout);
//        post("/game", gameService::createGame);
//        get("/game", gameService::listGames);
//        put("/game", gameService::joinGame);
//        delete("/db", clearService::clearAll);

        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body("Internal Server Error: " + e.getMessage());
        });

        return desiredPort;
    }

    public void stop() {
        stop();
        awaitStop();
    }
}
