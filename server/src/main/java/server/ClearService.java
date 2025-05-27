package server;

import dataaccess.AuthStorage;
import dataaccess.DataAccessException;
import dataaccess.GameStorage;
import dataaccess.UserStorage;
import spark.Request;
import spark.Response;

public class ClearService {
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
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            res.status(500);
            return "{\"message\": \"Database error: " + e.getMessage() + "\"}";
        }
    }
}
