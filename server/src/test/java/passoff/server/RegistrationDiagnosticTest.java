package passoff.server;

import dataaccess.SQLAuthStorage;
import dataaccess.SQLUserStorage;
import org.junit.jupiter.api.Test;
import server.UserReg;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationDiagnosticTest {

    @Test
    public void testUserRegistration() {
        SQLUserStorage userStorage = new SQLUserStorage();
        SQLAuthStorage authStorage = new SQLAuthStorage();
        UserReg userRegService = new UserReg(userStorage, authStorage);

        try {
            // Clean slate
            userStorage.clear();
            authStorage.clear();

            // Simulate a registration request
            String jsonRequest = "{\"username\":\"diagUser\",\"password\":\"pass123\",\"email\":\"diag@example.com\"}";
            Request dummyRequest = new Request() {
                @Override
                public String body() {
                    return jsonRequest;
                }
            };

            Response dummyResponse = new Response() {
                int code = 0;

                @Override
                public void status(int statusCode) {
                    this.code = statusCode;
                    System.out.println("[DEBUG] Dummy response status set to: " + code);
                }

                @Override
                public void type(String contentType) {
                    System.out.println("[DEBUG] Dummy response content type set to: " + contentType);
                }
            };

            System.out.println("[DEBUG] Attempting registration...");
            String result = userRegService.register(dummyRequest, dummyResponse);
            System.out.println("[DEBUG] Registration result: " + result);

            assertTrue(result.contains("authToken"));
            assertTrue(result.contains("diagUser"));

            // Extract token
            String token = result.split("authToken\":\"")[1].split("\"")[0];
            boolean isValid = authStorage.isValidToken(token);
            assertTrue(isValid, "[DEBUG] Token should be valid after registration.");
            System.out.println("[DEBUG] Token is valid: " + token);

        } catch (Exception e) {
            e.printStackTrace();
            fail("[ERROR] Exception during user registration diagnostic: " + e.getMessage());
        }
    }
}
