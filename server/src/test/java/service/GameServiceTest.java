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

    // Reusable Request class for testing
    static class TestRequest extends Request {
        private final String auth;
        private final String body;

        public TestRequest(String auth, String body) {
            this.auth = auth;
            this.body = body;
        }

        @Override
        public String headers(String name) {
            return "Authorization".equals(name) ? auth : null;
        }

        @Override
        public String body() {
            return body;
        }
    }

    // Reusable Response class for testing
    static class TestResponse extends Response {
        private int statusCode;
        private String responseBody;
        private String contentType;

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

        @Override
        public void type(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String type() {
            return contentType;
        }
    }

    @Test
    void joinGameSuccess() {
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

        TestRequest request = new TestRequest(authToken, requestBody);
        TestResponse response = new TestResponse();

        String result = gameService.joinGame(request, response);

        System.out.println("JoinGame Success Debug:");
        System.out.println("- Request Body: " + requestBody);
        System.out.println("- Response: " + result);
        System.out.println("- Status: " + response.status());

        assertEquals(200, response.status(), "Expected HTTP 200 for successful join");
        assertTrue(result.contains("{}"), "Expected empty JSON object");

        GameData game = gameStorage.getGame(gameID);
        assertEquals(username, game.getWhiteUsername(), "Expected white player to match username");
    }

    @Test
    void joinGameTakenColor() {
        String username1 = "alice";
        String username2 = "bob";
        String authToken1 = "token1";
        String authToken2 = "token2";
        int gameID = gameStorage.createGame("ConflictGame", username1);

        authStorage.addToken(authToken1, username1);
        authStorage.addToken(authToken2, username2);

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

        TestRequest req1 = new TestRequest(authToken1, body1);
        TestResponse res1 = new TestResponse();
        gameService.joinGame(req1, res1);

        TestRequest req2 = new TestRequest(authToken2, body2);
        TestResponse res2 = new TestResponse();
        String result = gameService.joinGame(req2, res2);

        System.out.println("JoinGame TakenColor Debug:");
        System.out.println("- Status: " + res2.status());
        System.out.println("- Result: " + result);

        assertEquals(403, res2.status(), "Expected HTTP 403 for taken color");
        assertTrue(result.contains("already taken"), "Expected error about taken spot");
    }

    @Test
    void joinGameInvalidGameID() {
        String username = "frank";
        String authToken = "auth999";
        authStorage.addToken(authToken, username);

        int validGameID = gameStorage.createGame("ValidGame1", username);
        int invalidGameID = validGameID + 1000;

        String body = String.format("""
        {
          "gameID": %d,
          "playerColor": "BLACK"
        }
        """, invalidGameID);

        TestRequest request = new TestRequest(authToken, body);
        TestResponse response = new TestResponse();

        String result = gameService.joinGame(request, response);

        System.out.println("JoinGame InvalidGameID Debug:");
        System.out.println("- Expected Invalid ID: " + invalidGameID);
        System.out.println("- Actual Status: " + response.status());
        System.out.println("- Result: " + result);

        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken") || result.contains("invalid"),
                "Expected error message for invalid game ID");
        assertEquals(403, response.status(), "Expected HTTP 403 for invalid game ID");
    }

    @Test
    void joinGameColorAlreadyTaken() {
        String gameName = "Clash of Colors";
        String creatorUsername = "alice";
        String joiningUsername = "bob";
        String creatorAuth = "tokenA";
        String joiningAuth = "tokenB";

        authStorage.addToken(creatorAuth, creatorUsername);
        authStorage.addToken(joiningAuth, joiningUsername);

        int gameID = gameStorage.createGame(gameName, creatorUsername);

        String whiteJoinBody = String.format("""
        {
          "gameID": %d,
          "playerColor": "WHITE"
        }
        """, gameID);

        TestRequest whiteRequest = new TestRequest(creatorAuth, whiteJoinBody);
        TestResponse whiteResponse = new TestResponse();
        gameService.joinGame(whiteRequest, whiteResponse);

        String conflictBody = String.format("""
        {
          "gameID": %d,
          "playerColor": "WHITE"
        }
        """, gameID);

        TestRequest conflictRequest = new TestRequest(joiningAuth, conflictBody);
        TestResponse conflictResponse = new TestResponse();
        String result = gameService.joinGame(conflictRequest, conflictResponse);

        System.out.println("JoinGame Conflict Response: " + result);
        System.out.println("Status Code: " + conflictResponse.status());

        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("already taken"), "Expected message about color already being taken");
        assertEquals(403, conflictResponse.status(), "Expected HTTP 403 for color conflict");
    }

    @Test
    void joinGameMissingAuth() {
        String gameName = "Unauthorized Game";
        String creatorUsername = "host";
        int gameID = gameStorage.createGame(gameName, creatorUsername);

        String requestBody = String.format("""
        {
          "gameID": %d,
          "playerColor": "BLACK"
        }
        """, gameID);

        Request request = new Request() {
            @Override
            public String body() {
                return requestBody;
            }

            @Override
            public String headers(String name) {
                return null;
            }
        };

        TestResponse response = new TestResponse();
        String result = gameService.joinGame(request, response);

        System.out.println("JoinGame_MissingAuth Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("unauthorized"), "Expected unauthorized error message");
        assertEquals(401, response.status(), "Expected HTTP 401 for missing auth");
    }

    @Test
    void createGameSuccess() {
        String requestBody = """
        {
          "gameName": "Epic Battle"
        }
        """;
        String authToken = "authToken-xyz";
        String username = "zeus";
        authStorage.addToken(authToken, username);

        TestRequest request = new TestRequest(authToken, requestBody);
        TestResponse response = new TestResponse();

        String result = gameService.createGame(request, response);

        System.out.println("createGame_Success Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        assertEquals(200, response.status(), "Expected 200 OK for valid game creation");
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("\"gameID\""), "Response should contain a gameID");
    }

    @Test
    void createGameBadRequest() {
        String requestBody = """
        {
          "gameName": ""
        }
        """;
        String authToken = "authToken-xyz";
        String username = "hera";
        authStorage.addToken(authToken, username);

        TestRequest request = new TestRequest(authToken, requestBody);
        TestResponse response = new TestResponse();

        String result = gameService.createGame(request, response);

        System.out.println("createGame_BadRequest Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        assertEquals(400, response.status(), "Expected 400 Bad Request for empty game name");
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("Error: bad request"), "Expected bad request message in response");
    }

    @Test
    void listGamesSuccess() {
        gameStorage.createGame("First Game", "alice");
        gameStorage.createGame("Second Game", "bob");

        String authToken = "auth-token-xyz";
        String username = "charlie";
        authStorage.addToken(authToken, username);

        Request request = new Request() {
            @Override
            public String headers(String name) {
                return "Authorization".equals(name) ? authToken : null;
            }
        };

        TestResponse response = new TestResponse();
        String result = gameService.listGames(request, response);

        System.out.println("listGames_Success Debug:");
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        assertEquals(200, response.status(), "Expected HTTP 200 for successful game listing");
        assertNotNull(result, "Response should not be null");
        assertTrue(result.contains("First Game"), "Expected to find 'First Game' in response");
        assertTrue(result.contains("Second Game"), "Expected to find 'Second Game' in response");
    }
}
