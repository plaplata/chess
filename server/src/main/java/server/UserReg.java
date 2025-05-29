package server;

import com.google.gson.Gson;
import dataaccess.AuthStorage;
import dataaccess.DataAccessException;
import dataaccess.UserStorage;
import model.AuthToken;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;

public class UserReg {
    public static Gson gson = new Gson();

    private final UserStorage userStorage;
    private final AuthStorage authStorage;

    public UserReg(UserStorage userStorage, AuthStorage authStorage) {
        this.userStorage = userStorage;
        this.authStorage = authStorage;
    }

    public String register(Request request, Response response) {
        try {
            User user = gson.fromJson(request.body(), User.class);

            if (user.username == null || user.password == null || user.email == null ||
                    user.username.isEmpty() || user.password.isEmpty() || user.email.isEmpty()) {
                if (response != null) response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            try {
                if (!userStorage.addUser(user.username, user.password, user.email)) {
                    if (response != null) response.status(403);
                    return gson.toJson(Map.of("message", "Error: already taken"));
                }
            } catch (DataAccessException e) {
                if (response != null) response.status(500);
                return gson.toJson(Map.of("message", "Error: database failure"));
            }

            String authToken = UUID.randomUUID().toString();

            System.out.println("[DEBUG] Registering user: " + user.username);
            System.out.println("[DEBUG] Generated token: " + authToken);

            try {
                authStorage.insertToken(new AuthToken(authToken, user.username));
            } catch (DataAccessException e) {
                if (response != null) response.status(500);
                return gson.toJson(Map.of("message", "Error: database failure"));
            }

            if (response != null) {
                response.status(200);
                response.type("application/json");
            }
            return gson.toJson(new AuthResponse(user.username, authToken));

        } catch (Exception e) {
            if (response != null) response.status(500);
            return gson.toJson(Map.of("message", "Unexpected error: " + e.getMessage()));
        }
    }




    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static class User {
        String username;
        String password;
        String email;
    }

    private static class AuthResponse {
        String username;
        String authToken;

        AuthResponse(String username, String authToken) {
            this.username = username;
            this.authToken = authToken;
        }
    }
}
