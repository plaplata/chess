package server;

import static spark.Spark.*;

public class Server {

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567);
    }

    public int run(int desiredPort) {
        port(desiredPort);

        staticFiles.location("web");

        get("/", (request, response) -> {
            // show something
            response.status(200);
            response.type("text/plain");
            response.header("CS240", "Awesome!");
            return "get";
        });

        post("/", (request, response) -> {
            // create something
            return "post";
        });

        put("/", (request, response) -> {
            // update something
            return "put";
        });

        delete("/", (request, response) -> {
            // delete something
            return "delete";
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
