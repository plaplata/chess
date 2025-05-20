package service;

import dataAccess.AuthMemoryStorage;
import dataAccess.GameMemoryStorage;
import dataAccess.UserMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ClearService;
import spark.Request;
import spark.Response;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClearServiceTest {

    private UserMemoryStorage userStorage;
    private AuthMemoryStorage authStorage;
    private GameMemoryStorage gameStorage;
    private ClearService clearService;
    private Request request;
    private Response response;

    @BeforeEach
    void setup() {
        userStorage = new UserMemoryStorage();
        authStorage = new AuthMemoryStorage();
        gameStorage = new GameMemoryStorage();
        clearService = new ClearService(userStorage, authStorage, gameStorage);

        request = mock(Request.class);
        response = mock(Response.class);

        // Insert some dummy data to verify clear functionality
        userStorage.addUser("bob", "123", "bob@bob.com");
        authStorage.addToken("authToken123", "bob");
        gameStorage.createGame("Bob’s Game", "bob");
    }

    @Test
    void clearAll_ClearsEverythingSuccessfully() {
        String result = clearService.clearAll(request, response);

        assertEquals("{}", result, "Expected empty JSON object after clearing");

        assertFalse(userStorage.validateCredentials("bob", "123"), "User data should be cleared");
        assertFalse(authStorage.isValidToken("authToken123"), "Auth token should be cleared");
        assertTrue(gameStorage.listGames().isEmpty(), "Games should be cleared");

        verify(response).status(200);
    }
}
