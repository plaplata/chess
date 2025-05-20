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
        UserReg userReg = new UserReg(users, auths);
        UserLogin userLogin = new UserLogin(users, auths);
        UserLogout userLogout = new UserLogout(auths);
        ClearService clearService = new ClearService(users, auths, games);

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
                if (authToken == null || !auths.isValidToken(authToken)) {
                    halt(401, "Error: unauthorized");
                }
            }
        });

        // Register routes
        post("/user", userReg::register);
        post("/session", userLogin::login);
        delete("/session", userLogout::logout);
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
