package server;

import com.google.gson.Gson;
import dataaccess.AuthStorage;
import spark.Request;
import spark.Response;

import java.util.Map;

public class UserLogout {
    private final AuthStorage authStorage;
    private final Gson gson = new Gson();

    public UserLogout(AuthStorage authStorage) {
        this.authStorage = authStorage;
    }

    public String logout(Request request, Response response) {
        String authToken = request.headers("Authorization");

        if (authToken == null || !authStorage.isValidToken(authToken)) {
            response.status(401);
            return gson.toJson(Map.of("message", "Error: unauthorized"));
        }

        authStorage.removeToken(authToken);
        response.status(200);
        response.type("application/json");
        return gson.toJson(Map.of("message", "logged out"));
    }
}
