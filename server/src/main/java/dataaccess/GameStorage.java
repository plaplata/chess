package dataaccess;

import java.util.List;
import service.GameData;

public interface GameStorage {
    int createGame(String gameName, String creatorUsername);
    List<GameData> listGames();
    GameData getGame(int gameID);
    boolean joinGame(int gameID, String username, String color);
    void clear();
}
