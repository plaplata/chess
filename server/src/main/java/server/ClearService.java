package server;

import dataaccess.AuthStorage;
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

    public Object clearAll(Request request, Response response) {
        users.clear();
        auths.clear();
        games.clear();

        response.status(200);
        response.type("application/json");
        return "{}";
    }
}
