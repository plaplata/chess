package passoff.server;

import dataaccess.AuthStorage;
import dataaccess.SQLAuthStorage;
import dataaccess.DataAccessException;
import model.AuthToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthStorageDiagnosticTest {

    private AuthStorage authStorage;

    @BeforeEach
    public void setup() {
        authStorage = new SQLAuthStorage();
    }

    @Test
    public void testAddAndValidateToken() {
        String username = "test_user_" + UUID.randomUUID();
        String token = null;

        // 1. Test insert
        try {
            token = authStorage.addToken(username);
            System.out.println("[DEBUG] Token generated and inserted: " + token);
            assertNotNull(token, "Token should not be null after insertion.");
        } catch (DataAccessException e) {
            fail("[FAIL] addToken threw DataAccessException: " + e.getMessage());
        }

        // 2. Test validation
        try {
            boolean isValid = authStorage.isValidToken(token);
            System.out.println("[DEBUG] Token validation result: " + isValid);
            assertTrue(isValid, "Token should be valid after insertion.");
        } catch (DataAccessException e) {
            fail("[FAIL] isValidToken threw DataAccessException: " + e.getMessage());
        }

        // 3. Test lookup
        try {
            String retrievedUser = authStorage.getUsernameByToken(token);
            System.out.println("[DEBUG] Username retrieved for token: " + retrievedUser);
            assertEquals(username, retrievedUser, "Username from token should match original.");
        } catch (DataAccessException e) {
            fail("[FAIL] getUsernameByToken threw DataAccessException: " + e.getMessage());
        }

        // 4. Clear and check invalidation
        try {
            System.out.println("[DEBUG] Attempting to clear auth table...");
            authStorage.clear();  // This should truncate or delete all auth rows
            boolean stillValid = authStorage.isValidToken(token);
            System.out.println("[DEBUG] Token validation after clear: " + stillValid);
            assertFalse(stillValid, "Token should no longer be valid after clearing.");
        } catch (DataAccessException e) {
            fail("[FAIL] clear or post-clear check threw DataAccessException: " + e.getMessage());
        }
    }

    @Test
    public void testAuthInsertDiagnostic() throws DataAccessException {
        SQLAuthStorage authStorage = new SQLAuthStorage();
        String token = UUID.randomUUID().toString();
        String username = "test_user_auth";

        System.out.println("[DEBUG] Attempting to insert token...");
        authStorage.insertToken(new AuthToken(token, username));

        System.out.println("[DEBUG] Token inserted: " + token);
        assertTrue(authStorage.isValidToken(token), "Token should be valid after insert");
    }

}
