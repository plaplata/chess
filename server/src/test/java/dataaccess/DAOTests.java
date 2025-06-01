package dataaccess;

import org.junit.jupiter.api.*;
import service.GameData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOTests {

    private SQLUserStorage users;
    private SQLAuthStorage auths;
    private SQLGameStorage games;

    @BeforeEach
    void setup() throws DataAccessException {
        users = new SQLUserStorage();
        auths = new SQLAuthStorage();
        games = new SQLGameStorage();
        users.clear();
        auths.clear();
        games.clear();
    }

    // ------------------------ User Tests ------------------------

    @Test
    @Order(1)
    public void addUserSuccess() throws DataAccessException {
        assertTrue(users.addUser("user1", "pass", "email"));
    }

    @Test
    @Order(2)
    public void addUserDuplicate() throws DataAccessException {
        users.addUser("user1", "pass", "email");
        assertFalse(users.addUser("user1", "pass", "email"));
    }

    @Test
    @Order(3)
    public void validateCredentialsSuccess() throws DataAccessException {
        users.addUser("user2", "mypassword", "mail");
        assertTrue(users.validateCredentials("user2", "mypassword"));
    }

    @Test
    @Order(4)
    public void validateCredentialsWrongPassword() throws DataAccessException {
        users.addUser("user2", "mypassword", "mail");
        assertFalse(users.validateCredentials("user2", "wrongpass"));
    }

    @Test
    @Order(5)
    public void clearUsers() throws DataAccessException {
        users.addUser("toClear", "123", "x");
        users.clear();
        assertFalse(users.validateCredentials("toClear", "123"));
    }

    // ------------------------ Auth Tests ------------------------

    @Test
    @Order(6)
    public void addTokenAndValidate() throws DataAccessException {
        String token = UUID.randomUUID().toString();
        auths.addToken(token, "userA");
        assertTrue(auths.isValidToken(token));
    }

    @Test
    @Order(7)
    public void getUsernameByTokenValid() throws DataAccessException {
        String token = UUID.randomUUID().toString();
        auths.addToken(token, "userA");
        assertEquals("userA", auths.getUsernameByToken(token));
    }

    @Test
    @Order(8)
    public void getUsernameByTokenInvalid() throws DataAccessException {
        String result = auths.getUsernameByToken("badtoken");
        assertNull(result, "Expected null for nonexistent auth token");
    }

    @Test
    @Order(9)
    public void removeTokenSuccess() throws DataAccessException {
        String token = UUID.randomUUID().toString();
        auths.addToken(token, "userB");
        auths.removeToken(token);
        assertFalse(auths.isValidToken(token));
    }

    @Test
    @Order(10)
    public void clearAuths() throws DataAccessException {
        auths.addToken("t1", "u");
        auths.clear();
        assertFalse(auths.isValidToken("t1"));
    }

    // ------------------------ Game Tests ------------------------

    @Test
    @Order(11)
    public void createGameSuccess() throws DataAccessException {
        int id = games.createGame("Match", "creator");
        assertTrue(id > 0);
    }

    @Test
    @Order(12)
    public void joinGameSuccess() throws DataAccessException {
        int id = games.createGame("M", "c");
        boolean joined = games.joinGame(id, "player", "WHITE");
        assertTrue(joined);
    }

    @Test
    @Order(13)
    public void joinGameAlreadyTaken() throws DataAccessException {
        int id = games.createGame("M", "c");
        games.joinGame(id, "one", "WHITE");
        assertThrows(DataAccessException.class, () -> games.joinGame(id, "two", "WHITE"));
    }

    @Test
    @Order(14)
    public void listGames() throws DataAccessException {
        games.createGame("G1", "a");
        games.createGame("G2", "b");
        List<GameData> all = games.listGames();
        assertEquals(2, all.size());
    }

    @Test
    @Order(15)
    public void clearGames() throws DataAccessException {
        games.createGame("will be cleared", "z");
        games.clear();
        List<GameData> empty = games.listGames();
        assertTrue(empty.isEmpty());
    }
}
