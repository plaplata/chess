package server;

import com.google.gson.Gson;
import dataaccess.AuthStorage;
import dataaccess.DataAccessException;
import dataaccess.SQLUserStorage;
import dataaccess.UserStorage;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;

public class UserLogin {
    private final UserStorage userStorage;
    private final AuthStorage authStorage;
    private final Gson gson = new Gson();

    public UserLogin(UserStorage userStorage, AuthStorage authStorage) {
        this.userStorage = userStorage;
        this.authStorage = authStorage;
    }

    public String login(Request request, Response response) {
        try {
            User user = gson.fromJson(request.body(), User.class);

            if (user.username == null || user.password == null ||
                    user.username.isEmpty() || user.password.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            if (!userStorage.validateCredentials(user.username, user.password)) {
                if (userStorage instanceof SQLUserStorage sqlStore && sqlStore.wasConnectionError()) {
                    response.status(500);
                    return gson.toJson(Map.of("message", "Error: failed to connect to database"));
                }

                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }


            String authToken = UUID.randomUUID().toString();
            authStorage.addToken(authToken, user.username);

            response.status(200);
            response.type("application/json");
            return gson.toJson(new AuthResponse(user.username, authToken));

        } catch (DataAccessException e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }


    private static class User {
        String username;
        String password;
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
