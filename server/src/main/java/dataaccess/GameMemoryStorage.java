package dataaccess;

import java.util.HashMap;
import java.util.Map;

public class GameMemoryStorage implements GameStorage {
    private final Map<Integer, String> games = new HashMap<>();

    @Override
    public void clear() {
        games.clear();
    }
}
