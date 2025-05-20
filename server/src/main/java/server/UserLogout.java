package server;

import dataaccess.AuthStorage;
import spark.Request;
import spark.Response;

import java.util.Map;

public class UserLogout {

    private final AuthStorage authStorage;

    public UserLogout(AuthStorage authStorage) {
        this.authStorage = authStorage;
    }

    public String logout(Request request, Response response) {
        String authToken = request.headers("Authorization");

        if (authToken == null || !authStorage.isValidToken(authToken)) {
            response.status(401);
            return toJson("Error: unauthorized");
        }

        authStorage.removeToken(authToken);
        response.status(200);
        response.type("application/json");
        return "{}";
    }

    private String toJson(String message) {
        return String.format("{\"message\":\"%s\"}", message);
    }
}
