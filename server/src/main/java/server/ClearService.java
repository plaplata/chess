package server;

import dataAccess.AuthStorage;
import dataAccess.GameStorage;
import dataAccess.UserStorage;
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
        users.clear();
        auths.clear();
        games.clear();

        res.status(200);
        return "{}";
    }
}
