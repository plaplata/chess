package dataaccess;

import java.util.HashSet;
import java.util.Set;

public class AuthMemoryStorage implements AuthStorage {
    private final Set<String> tokens = new HashSet<>();

    @Override
    public void clear() {
        tokens.clear();
    }
}
