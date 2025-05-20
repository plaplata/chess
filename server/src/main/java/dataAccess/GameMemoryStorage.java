package dataAccess;

import service.GameData;

import java.util.*;

public class GameMemoryStorage implements GameStorage {

    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName, String creatorUsername) {
        int id = nextGameID++;
        games.put(id, new GameData(id, gameName, null, null));
        return id;
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public boolean joinGame(int gameID, String username, String color) {
        GameData game = games.get(gameID);
        if (game == null) return false;

        switch (color.toLowerCase()) {
            case "white":
                if (game.getWhiteUsername() != null) return false;
                game.setWhiteUsername(username);
                return true;
            case "black":
                if (game.getBlackUsername() != null) return false;
                game.setBlackUsername(username);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void clear() {
        games.clear();
        nextGameID = 1;
    }
}
