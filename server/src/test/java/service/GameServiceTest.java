package service;

import dataaccess.AuthMemoryStorage;
import dataaccess.GameMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.GameService;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

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

    // Minimal request class
    static class SimpleRequest extends Request {
        private final String auth;
        private final String body;

        public SimpleRequest(String auth, String body) {
            this.auth = auth;
            this.body = body;
        }

        @Override
        public String headers(String name) {
            if ("Authorization".equals(name)) {
                return auth;
            }
            return null;
        }

        @Override
        public String body() {
            return body;
        }
    }

    // Minimal response class
    static class SimpleResponse extends Response {
        private int status;
        private String body;

        @Override
        public void status(int statusCode) {
            this.status = statusCode;
        }

        @Override
        public int status() {
            return status;
        }

        @Override
        public void body(String value) {
            this.body = value;
        }

        @Override
        public String body() {
            return body;
        }
    }

    @Test
    void joinGameSuccess() {
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

        SimpleRequest request = new SimpleRequest(authToken, requestBody);
        SimpleResponse response = new SimpleResponse();

        // Act
        String result = gameService.joinGame(request, response);

        // Debug
        System.out.println("JoinGame Success Debug:");
        System.out.println("- Request Body: " + requestBody);
        System.out.println("- Response: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertEquals(200, response.status(), "Expected HTTP 200 for successful join");
        assertTrue(result.contains("{}"), "Expected empty JSON object");

        GameData game = gameStorage.getGame(gameID);
        assertEquals(username, game.getWhiteUsername(), "Expected white player to match username");
    }

    @Test
    void joinGameTakenColor() {
        // Arrange
        String username1 = "alice";
        String username2 = "bob";
        String authToken1 = "token1";
        String authToken2 = "token2";

        int gameID = gameStorage.createGame("ConflictGame", username1);

        // Register both users
        authStorage.addToken(authToken1, username1);
        authStorage.addToken(authToken2, username2);

        // First user joins as BLACK
        String body1 = String.format("""
    {
      "gameID": %d,
      "playerColor": "BLACK"
    }
    """, gameID);
        String body2 = String.format("""
    {
      "gameID": %d,
      "playerColor": "BLACK"
    }
    """, gameID);

        // First join succeeds
        SimpleRequest req1 = new SimpleRequest(authToken1, body1);
        SimpleResponse res1 = new SimpleResponse();
        gameService.joinGame(req1, res1);

        // Second join fails due to taken color
        SimpleRequest req2 = new SimpleRequest(authToken2, body2);
        SimpleResponse res2 = new SimpleResponse();
        String result = gameService.joinGame(req2, res2);

        // Debug
        System.out.println("JoinGame TakenColor Debug:");
        System.out.println("- Status: " + res2.status());
        System.out.println("- Result: " + result);

        // Assert
        assertEquals(403, res2.status(), "Expected HTTP 403 for taken color");
        assertTrue(result.contains("already taken"), "Expected error about taken spot");
    }

    @Test
    void joinGameInvalidGameID() {
        // Arrange
        String username = "frank";
        String authToken = "auth999";
        authStorage.addToken(authToken, username);

        // Create a valid game and set invalid ID to something way higher
        int validGameID = gameStorage.createGame("ValidGame1", username);
        int invalidGameID = validGameID + 1000;

        String body = String.format("""
    {
      "gameID": %d,
      "playerColor": "BLACK"
    }
    """, invalidGameID);

        SimpleRequest request = new SimpleRequest(authToken, body);
        SimpleResponse response = new SimpleResponse();

        // Act
        String result = gameService.joinGame(request, response);

        // Debug
        System.out.println("JoinGame InvalidGameID Debug:");
        System.out.println("- Expected Invalid ID: " + invalidGameID);
        System.out.println("- Actual Status: " + response.status());
        System.out.println("- Result: " + result);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken") || result.contains("invalid"),
                "Expected error message for invalid game ID");
        assertEquals(403, response.status(), "Expected HTTP 403 for invalid game ID");
    }

    @Test
    void joinGameColorAlreadyTaken() {
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

        class SimpleRequest extends Request {
            final String body;
            final String auth;
            SimpleRequest(String body, String auth) {
                this.body = body;
                this.auth = auth;
            }
            @Override public String body() { return body; }
            @Override public String headers(String name) {
                return "Authorization".equals(name) ? auth : null;
            }
        }

        class SimpleResponse extends Response {
            private int status;
            private String type;
            @Override public void status(int statusCode) { this.status = statusCode; }
            @Override public int status() { return status; }
            @Override public void type(String contentType) { this.type = contentType; }
            @Override public String type() { return type; }
        }

        SimpleRequest whiteRequest = new SimpleRequest(whiteJoinBody, creatorAuth);
        SimpleResponse whiteResponse = new SimpleResponse();
        gameService.joinGame(whiteRequest, whiteResponse);

        // Bob tries to join as WHITE (already taken)
        String conflictBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "WHITE"
    }
    """, gameID);

        SimpleRequest conflictRequest = new SimpleRequest(conflictBody, joiningAuth);
        SimpleResponse conflictResponse = new SimpleResponse();

        // Act
        String result = gameService.joinGame(conflictRequest, conflictResponse);

        // Debug
        System.out.println("JoinGame Conflict Response: " + result);
        System.out.println("Status Code: " + conflictResponse.status());

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken"), "Expected message about color already being taken");
        assertEquals(403, conflictResponse.status(), "Expected HTTP 403 for color conflict");
    }

    @Test
    void joinGameMissingAuth() {
        // Arrange
        String gameName = "Unauthorized Game";
        String creatorUsername = "host";
        int gameID = gameStorage.createGame(gameName, creatorUsername);

        String requestBody = String.format("""
    {
      "gameID": %d,
      "playerColor": "BLACK"
    }
    """, gameID);

        // Custom Request with NO Authorization header
        Request request = new Request() {
            @Override
            public String body() {
                return requestBody;
            }

            @Override
            public String headers(String name) {
                return null; // No token
            }
        };

        // Minimal Response implementation
        class SimpleResponse extends Response {
            private int status;
            private String body;

            @Override
            public void status(int statusCode) {
                this.status = statusCode;
            }

            @Override
            public int status() {
                return status;
            }

            @Override
            public void body(String body) {
                this.body = body;
            }

            @Override
            public String body() {
                return body;
            }
        }

        SimpleResponse response = new SimpleResponse();

        // Act
        String result = gameService.joinGame(request, response);

        // Debug
        System.out.println("JoinGame_MissingAuth Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("unauthorized"), "Expected unauthorized error message");
        assertEquals(401, response.status(), "Expected HTTP 401 for missing auth");
    }

    @Test
    void createGameSuccess() {
        // Arrange
        String requestBody = """
        {
          "gameName": "Epic Battle"
        }
    """;
        String authToken = "authToken-xyz";
        String username = "zeus";
        authStorage.addToken(authToken, username);

        // Custom Request and Response
        Request request = new Request() {
            @Override
            public String body() {
                return requestBody;
            }

            @Override
            public String headers(String header) {
                if ("Authorization".equals(header)) {
                    return authToken;
                }
                return null;
            }
        };

        class SimpleResponse extends Response {
            int statusCode;
            String responseBody;

            @Override
            public void status(int statusCode) {
                this.statusCode = statusCode;
            }

            @Override
            public int status() {
                return statusCode;
            }

            @Override
            public void body(String body) {
                this.responseBody = body;
            }

            @Override
            public String body() {
                return responseBody;
            }
        }

        SimpleResponse response = new SimpleResponse();

        // Act
        String result = gameService.createGame(request, response);

        // Debug
        System.out.println("createGame_Success Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertEquals(200, response.status(), "Expected 200 OK for valid game creation");
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("\"gameID\""), "Response should contain a gameID");
    }

    @Test
    void createGameBadRequest() {
        // Arrange: invalid input (empty gameName)
        String requestBody = """
        {
          "gameName": ""
        }
    """;
        String authToken = "authToken-xyz";
        String username = "hera";
        authStorage.addToken(authToken, username);

        // Custom Request and Response
        Request request = new Request() {
            @Override
            public String body() {
                return requestBody;
            }

            @Override
            public String headers(String header) {
                if ("Authorization".equals(header)) {
                    return authToken;
                }
                return null;
            }
        };

        class SimpleResponse extends Response {
            int statusCode;
            String responseBody;

            @Override
            public void status(int statusCode) {
                this.statusCode = statusCode;
            }

            @Override
            public int status() {
                return statusCode;
            }

            @Override
            public void body(String body) {
                this.responseBody = body;
            }

            @Override
            public String body() {
                return responseBody;
            }
        }

        SimpleResponse response = new SimpleResponse();

        // Act
        String result = gameService.createGame(request, response);

        // Debug
        System.out.println("createGame_BadRequest Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertEquals(400, response.status(), "Expected 400 Bad Request for empty game name");
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("Error: bad request"), "Expected bad request message in response");
    }

    @Test
    void listGamesSuccess() {
        // Arrange: add two games manually
        gameStorage.createGame("First Game", "alice");
        gameStorage.createGame("Second Game", "bob");

        String authToken = "auth-token-xyz";
        String username = "charlie";
        authStorage.addToken(authToken, username);

        Request request = new Request() {
            @Override
            public String headers(String name) {
                if ("Authorization".equals(name)) {
                    return authToken;
                }
                return null;
            }
        };

        class SimpleResponse extends Response {
            int statusCode;
            String responseBody;

            @Override
            public void status(int statusCode) {
                this.statusCode = statusCode;
            }

            @Override
            public int status() {
                return statusCode;
            }

            @Override
            public void body(String body) {
                this.responseBody = body;
            }

            @Override
            public String body() {
                return responseBody;
            }
        }

        SimpleResponse response = new SimpleResponse();

        // Act
        String result = gameService.listGames(request, response);

        // Debug
        System.out.println("listGames_Success Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertEquals(200, response.status(), "Expected HTTP 200 for successful game listing");
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("First Game"), "Expected to find 'First Game' in response");
        assertTrue(result.contains("Second Game"), "Expected to find 'Second Game' in response");
    }

}
