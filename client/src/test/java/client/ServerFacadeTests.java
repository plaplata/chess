package client;

import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static String uniqueUser;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade("localhost", port);
        uniqueUser = "user" + System.currentTimeMillis();
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerPositive() throws IOException {
        AuthResponse res = facade.register(uniqueUser, "pass123", uniqueUser + "@mail.com");
        assertNotNull(res);
        assertNotNull(res.authToken);
        assertEquals(uniqueUser, res.username);
    }

    @Test
    public void registerNegative() {
        assertThrows(IOException.class, () -> {
            facade.register("", "", "");
        });
    }

    @Test
    public void loginPositive() throws IOException {
        String username = uniqueUser + "2";
        facade.register(username, "pass123", username + "@mail.com");
        AuthResponse res = facade.login(username, "pass123");
        assertNotNull(res.authToken);
        assertEquals(username, res.username);
    }

    @Test
    public void loginNegative() {
        assertThrows(IOException.class, () -> {
            facade.login("no_such_user", "wrong_pass");
        });
    }

    @Test
    public void createGamePositive() throws IOException {
        String username = uniqueUser + "3";
        AuthResponse res = facade.register(username, "pass123", username + "@mail.com");
        CreateGameResponse gameRes = facade.createGame(res.authToken, "TestGame");
        assertNotNull(gameRes);
        assertTrue(gameRes.gameID > 0);
    }

    @Test
    public void createGameNegative() {
        assertThrows(IOException.class, () -> {
            facade.createGame("invalid_token", "Game");
        });
    }

    @Test
    public void listGamesPositive() throws IOException {
        String username = uniqueUser + "4";
        AuthResponse res = facade.register(username, "pass123", username + "@mail.com");
        CreateGameResponse gameRes = facade.createGame(res.authToken, "ListGame");
        ListGamesResponse list = facade.listGames(res.authToken);
        assertNotNull(list.games);
        assertTrue(list.games.stream().anyMatch(g -> g.gameID == gameRes.gameID));
    }

    @Test
    public void listGamesNegative() {
        assertThrows(IOException.class, () -> {
            facade.listGames("invalid_token");
        });
    }

    //joinGamePositive here

    @Test
    public void joinGameNegative() {
        assertThrows(IOException.class, () -> {
            facade.joinGame("invalid_token", 9999, "BLACK");
        });
    }

    @Test
    public void logoutPositive() throws IOException {
        String username = uniqueUser + "6";
        AuthResponse res = facade.register(username, "pass123", username + "@mail.com");
        facade.logout(res.authToken);
        // No exception = pass
    }

    @Test
    public void logoutNegative() {
        assertThrows(IOException.class, () -> {
            facade.logout("invalid_token");
        });
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }
}
