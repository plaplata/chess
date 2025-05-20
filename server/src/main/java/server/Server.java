package server;

import static spark.Spark.*;

import com.google.gson.Gson;
import dataaccess.AuthMemoryStorage;
import dataaccess.GameMemoryStorage;
import dataaccess.UserMemoryStorage;
import server.ClearService;

public class Server {

    public static Gson gson = new Gson();

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567);
    }

    public int run(int desiredPort) {
        port(desiredPort);
        staticFiles.location("/web");

        UserMemoryStorage users = new UserMemoryStorage();
        AuthMemoryStorage auths = new AuthMemoryStorage();
        GameMemoryStorage games = new GameMemoryStorage();

        // Register services
        UserReg userReg = new UserReg(users);
        ClearService clearService = new ClearService(users, auths, games);

        // Register routes
        post("/user", userReg::register);
        delete("/db", (req, res) -> clearService.clearAll(req, res));

        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body("Internal Server Error: " + e.getMessage());
        });

        init();
        awaitInitialization();

        return port();
    }

    public void stop() {
        stop();
        awaitStop();
    }
}
