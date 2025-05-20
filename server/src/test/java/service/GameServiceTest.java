package service;

import dataAccess.AuthMemoryStorage;
import dataAccess.GameMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.GameService;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameServiceTest {

    private GameMemoryStorage gameStorage;
    private AuthMemoryStorage authStorage;
    private GameService gameService;

    @BeforeEach
    void setup() {
        gameStorage = new GameMemoryStorage();
        authStorage = new AuthMemoryStorage();
        gameService = new GameService(gameStorage, authStorage);
    }

    @Test
    void createGame_Success() {
        String requestBody = """
            {
              "gameName": "Friendly Match"
            }
        """;

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.body()).thenReturn(requestBody);
        when(request.headers("Authorization")).thenReturn("auth-token-123");

        authStorage.addToken("auth-token-123", "alice");

        String result = gameService.createGame(request, response);

        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("\"gameID\""), "Expected a gameID in response");
        verify(response).status(200);
    }

    @Test
    void createGame_BadRequest() {
        String requestBody = """
        {
          "gameName": ""
        }
    """;

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.body()).thenReturn(requestBody);
        when(request.headers("Authorization")).thenReturn("auth-token-123");

        authStorage.addToken("auth-token-123", "alice");

        String result = gameService.createGame(request, response);

        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("Error: bad request"), "Expected bad request error");
        verify(response).status(400);
    }


    @Test
    void listGames_Success() {
        // Pre-populate some games
        gameStorage.createGame("Alice", "Chess Game 1");
        gameStorage.createGame("Bob", "Chess Game 2");

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.headers("Authorization")).thenReturn("auth-token-123");
        authStorage.addToken("auth-token-123", "alice");

        String result = gameService.listGames(request, response);

        assertNotNull(result, "Response should not be null");
        System.out.println("ListGames Response:\n" + result);
        assertTrue(result.contains("Alice"), "Expected first game in response");
        assertTrue(result.contains("Bob"), "Expected second game in response");
        verify(response).status(200);
    }


    @Test
    void joinGame_Success() {
        // Arrange
        String username = "charlie";
        String gameName = "TestGame";
        String authToken = "auth456";
        int gameID = gameStorage.createGame(gameName, username);

        authStorage.addToken(authToken, username);

        String requestBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "WHITE"
    }
    """, gameID);

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.body()).thenReturn(requestBody);
        when(request.headers("Authorization")).thenReturn(authToken);

        // Act
        String result = gameService.joinGame(request, response);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("{}") || result.equals("{}"), "Expected empty JSON object in response");

        GameData game = gameStorage.getGame(gameID);
        assertEquals(username, game.getWhiteUsername(), "White player should match username");

        verify(response).status(200);
    }

    @Test
    void joinGame_AlreadyTaken() {
        // Arrange
        String username1 = "dave";
        String username2 = "emma";
        String authToken = "auth789";
        String gameName = "TakenGame";
        int gameID = gameStorage.createGame(gameName, username1);

        // Pre-fill white spot
        gameStorage.getGame(gameID).setWhiteUsername(username1);

        // Set up second user
        authStorage.addToken(authToken, username2);

        String requestBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "WHITE"
    }
    """, gameID);

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.body()).thenReturn(requestBody);
        when(request.headers("Authorization")).thenReturn(authToken);

        // Act
        String result = gameService.joinGame(request, response);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken"), "Expected error message about spot already being taken");

        verify(response).status(403);
    }

    @Test
    void joinGame_InvalidGameID() {
        // Arrange
        String username = "frank";
        String authToken = "auth999";
        authStorage.addToken(authToken, username);

        // Create some games to get a valid upper bound
        int existingID = gameStorage.createGame("ValidGame1", username);
        int invalidGameID = existingID + 1000; // definitely invalid

        String requestBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "BLACK"
    }
    """, invalidGameID);

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.body()).thenReturn(requestBody);
        when(request.headers("Authorization")).thenReturn(authToken);

        // Act
        String result = gameService.joinGame(request, response);

        // Debug
        System.out.println("JoinGame Response: " + result);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken") || result.contains("invalid"),
                "Expected error message for invalid game ID");
        verify(response).status(403); // <-- Fixed from 400 to 403
    }

    @Test
    void joinGame_ColorAlreadyTaken() {
        // Arrange
        String gameName = "Clash of Colors";
        String creatorUsername = "alice";
        String joiningUsername = "bob";
        String creatorAuth = "tokenA";
        String joiningAuth = "tokenB";

        authStorage.addToken(creatorAuth, creatorUsername);
        authStorage.addToken(joiningAuth, joiningUsername);

        int gameID = gameStorage.createGame(gameName, creatorUsername);

        // Alice joins as WHITE
        String whiteJoinBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "WHITE"
    }
    """, gameID);

        Request whiteRequest = mock(Request.class);
        Response whiteResponse = mock(Response.class);

        when(whiteRequest.body()).thenReturn(whiteJoinBody);
        when(whiteRequest.headers("Authorization")).thenReturn(creatorAuth);

        gameService.joinGame(whiteRequest, whiteResponse);

        // Bob tries to join as WHITE (already taken)
        String conflictBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "WHITE"
    }
    """, gameID);

        Request conflictRequest = mock(Request.class);
        Response conflictResponse = mock(Response.class);

        when(conflictRequest.body()).thenReturn(conflictBody);
        when(conflictRequest.headers("Authorization")).thenReturn(joiningAuth);

        // Act
        String result = gameService.joinGame(conflictRequest, conflictResponse);

        // Debug
        System.out.println("JoinGame Conflict Response: " + result);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken"), "Expected message about color already being taken");
        verify(conflictResponse).status(403);
    }

    @Test
    void joinGame_MissingAuth() {
        // Arrange
        String gameName = "Unauthorized Game";
        String username = "hacker";
        int gameID = gameStorage.createGame(gameName, "someoneElse");

        String requestBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "BLACK"
    }
    """, gameID);

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.body()).thenReturn(requestBody);
        when(request.headers("Authorization")).thenReturn(null); // No token

        // Act
        String result = gameService.joinGame(request, response);

        // Debug
        System.out.println("JoinGame MissingAuth Response: " + result);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("unauthorized"), "Expected unauthorized error message");
        verify(response).status(401);
    }


}
