package server;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Set;

public class Server {

    public static Set<String> tokens = new HashSet<>();
    public static Gson gson = new Gson();
    //private static final UserService userService = new UserReg();

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567);
    }

    public int run(int desiredPort) {
        port(desiredPort);

        staticFiles.location("web");

        before((request, response) -> {
            String path = request.pathInfo();
            if (!path.equals("/user")) {
                String authToken = request.headers("Authorization");
                boolean authenticated = authToken != null && tokens.contains(authToken);
                if (!authenticated) {
                    halt(401, "You are not welcome here");
                }
            }
        });

        post("/user", (request, response) -> UserReg.register(request, response));

        exception(Exception.class, (exception, req, res) -> {
            res.status(500);
            res.body("Internal Server Error: " + exception.getMessage());


        });
        return desiredPort;
    }

    public void stop() {
        stop();
        awaitStop();
    }
}
