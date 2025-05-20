package service;

import dataAccess.AuthMemoryStorage;
import dataAccess.GameMemoryStorage;
import dataAccess.UserMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ClearService;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private UserMemoryStorage userStorage;
    private AuthMemoryStorage authStorage;
    private GameMemoryStorage gameStorage;
    private ClearService clearService;

    @BeforeEach
    void setup() {
        userStorage = new UserMemoryStorage();
        authStorage = new AuthMemoryStorage();
        gameStorage = new GameMemoryStorage();
        clearService = new ClearService(userStorage, authStorage, gameStorage);

        // Add some data to be cleared
        userStorage.addUser("alice", "pass", "alice@example.com");
        authStorage.addToken("token123", "alice");
        gameStorage.createGame("Game 1", "alice");
    }

    // Simple mock-free Request and Response
    static class DummyRequest extends Request {}
    static class DummyResponse extends Response {
        private int statusCode = 0;

        @Override
        public void status(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public int status() {
            return statusCode;
        }
    }

    @Test
    void clearAll_Positive() {
        DummyRequest request = new DummyRequest();
        DummyResponse response = new DummyResponse();

        String result = clearService.clearAll(request, response);

        // Debug
        System.out.println("ClearService Response: " + result);
        System.out.println("Status: " + response.status());

        assertEquals(200, response.status(), "Expected HTTP 200");
        assertTrue(result.contains("{}"), "Expected empty JSON response");
        assertFalse(authStorage.isValidToken("token123"), "Auth storage should be cleared");
        assertNull(gameStorage.getGame(1), "Game storage should be cleared");
        assertFalse(userStorage.validateCredentials("alice", "pass"), "User storage should be cleared");
    }
}
