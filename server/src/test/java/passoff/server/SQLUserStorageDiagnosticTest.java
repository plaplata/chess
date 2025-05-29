package passoff.server;

import dataaccess.SQLUserStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserStorageDiagnosticTest {

    @Test
    public void testAddAndValidateUser() throws Exception{
        SQLUserStorage userStorage = new SQLUserStorage();
        String username = "test_user_storage";
        String password = "test_password";
        String email = "test@example.com";

        System.out.println("[DEBUG] Attempting to add user...");
        boolean added = userStorage.addUser(username, password, email);
        System.out.println("[DEBUG] User added: " + added);
        assertTrue(added, "User should be added successfully");

        System.out.println("[DEBUG] Attempting to validate credentials...");
        boolean valid = userStorage.validateCredentials(username, password);
        System.out.println("[DEBUG] Credentials valid: " + valid);
        assertTrue(valid, "Credentials should be valid");

        System.out.println("[DEBUG] Attempting to clear users...");
        userStorage.clear();
        boolean stillValid = userStorage.validateCredentials(username, password);
        System.out.println("[DEBUG] Credentials after clear: " + stillValid);
        assertFalse(stillValid, "Credentials should be invalid after clear");
    }
}
