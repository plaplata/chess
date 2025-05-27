package dataaccess;

import java.util.List;
import service.GameData;

public interface GameStorage {
    int createGame(String gameName, String creatorUsername) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    boolean joinGame(int gameID, String username, String color) throws DataAccessException;
    void clear() throws DataAccessException;
}
