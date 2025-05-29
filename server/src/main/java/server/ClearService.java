package server;

import com.google.gson.Gson;
import dataaccess.AuthStorage;
import dataaccess.DataAccessException;
import dataaccess.GameStorage;
import dataaccess.UserStorage;
import spark.Request;
import spark.Response;

import java.util.Map;

public class ClearService {
    private final Gson gson = new Gson();
    private final UserStorage users;
    private final AuthStorage auths;
    private final GameStorage games;

    public ClearService(UserStorage users, AuthStorage auths, GameStorage games) {
        this.users = users;
        this.auths = auths;
        this.games = games;
    }

    public String clearAll(Request req, Response res) {
        try {
            users.clear();
            auths.clear();
            games.clear();
            if (res != null) res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            if (res != null) res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }


}
