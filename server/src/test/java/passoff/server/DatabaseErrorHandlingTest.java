
package passoff.server;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import passoff.model.*;
import server.Server;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseErrorHandlingTest {

    private static final TestUser TEST_USER = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");
    private static TestServerFacade serverFacade;
    private static Server server;
    private static Class<?> databaseManagerClass;

    @BeforeAll
    public static void startServer() {
        server = new Server();
        var port = server.run(0);
        System.out.println("[DEBUG] Started test HTTP server on port: " + port);
        serverFacade = new TestServerFacade("localhost", Integer.toString(port));
    }

    @AfterAll
    static void stopServer() {
        server.stop();
        System.out.println("[DEBUG] Server stopped.");
    }

    @Test
    @DisplayName("Database Error Handling - Probe Mode")
    @Order(1)
    public void databaseErrorHandling() throws ReflectiveOperationException {
        Properties fakeDbProperties = new Properties();
        fakeDbProperties.setProperty("db.name", UUID.randomUUID().toString());
        fakeDbProperties.setProperty("db.user", UUID.randomUUID().toString());
        fakeDbProperties.setProperty("db.password", UUID.randomUUID().toString());
        fakeDbProperties.setProperty("db.host", "localhost");
        fakeDbProperties.setProperty("db.port", "100000");

        databaseManagerClass = findDatabaseManager();
        Method loadPropertiesMethod = databaseManagerClass.getDeclaredMethod("loadProperties", Properties.class);
        loadPropertiesMethod.setAccessible(true);
        Object dbManagerInstance = databaseManagerClass.getDeclaredConstructor().newInstance();
        loadPropertiesMethod.invoke(dbManagerInstance, fakeDbProperties);
        System.out.println("[DEBUG] Fake DB properties loaded.");

        Map<String, Supplier<TestResult>> probes = new LinkedHashMap<>();
        probes.put("clear() → likely ClearService / SQLUserStorage", () -> serverFacade.clear());
        probes.put("register() → likely UserReg / SQLUserStorage", () -> serverFacade.register(TEST_USER));
        probes.put("login() → likely UserLogin / SQLUserStorage", () -> serverFacade.login(TEST_USER));
        probes.put("logout() → likely UserLogout / SQLAuthStorage", () -> serverFacade.logout(UUID.randomUUID().toString()));
        probes.put("createGame() → likely GameService / SQLGameStorage", () -> serverFacade.createGame(new TestCreateRequest("inaccessible"), UUID.randomUUID().toString()));
        probes.put("listGames() → likely GameService / SQLGameStorage", () -> serverFacade.listGames(UUID.randomUUID().toString()));
        probes.put("joinPlayer() → likely GameService / SQLGameStorage", () -> serverFacade.joinPlayer(new TestJoinRequest(ChessGame.TeamColor.WHITE, 1), UUID.randomUUID().toString()));

        for (Map.Entry<String, Supplier<TestResult>> entry : probes.entrySet()) {
            String label = entry.getKey();
            System.out.println("\n[PROBE] Running: " + label);
            try {
                TestResult result = entry.getValue().get();
                int status = serverFacade.getStatusCode();
                System.out.println("[PROBE] → Status Code: " + status);
                System.out.println("[PROBE] → Message: " + result.getMessage());

                if (status != 500) {
                    System.out.printf("[ALERT] %s returned %d instead of 500 — Check related files!\n", label, status);
                }
            } catch (Exception ex) {
                System.out.println("[ERROR] Exception during: " + label);
                ex.printStackTrace();
            }
        }

        Method resetMethod = databaseManagerClass.getDeclaredMethod("loadPropertiesFromResources");
        resetMethod.setAccessible(true);
        resetMethod.invoke(dbManagerInstance);
        System.out.println("[DEBUG] DB properties restored to original.");
    }


    private Class<?> findDatabaseManager() throws ClassNotFoundException {
        if (databaseManagerClass != null) return databaseManagerClass;
        for (Package p : getClass().getClassLoader().getDefinedPackages()) {
            try {
                Class<?> clazz = Class.forName(p.getName() + ".DatabaseManager");
                clazz.getDeclaredMethod("getConnection");
                databaseManagerClass = clazz;
                return clazz;
            } catch (ReflectiveOperationException ignored) {}
        }
        throw new ClassNotFoundException("Could not find DatabaseManager class with getConnection() method.");
    }
}
