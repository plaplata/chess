package service;

import dataaccess.AuthMemoryStorage;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.UserLogout;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

public class UserLogoutTest {

    private AuthMemoryStorage authStorage;
    private UserLogout logoutService;

    @BeforeEach
    void setup() {
        authStorage = new AuthMemoryStorage();
        logoutService = new UserLogout(authStorage);
    }

    // Custom minimal Request implementation
    static class SimpleRequest extends Request {
        private final String token;

        public SimpleRequest(String token) {
            this.token = token;
        }

        @Override
        public String headers(String name) {
            return "Authorization".equals(name) ? token : null;
        }
    }

    // Custom minimal Response implementation (with 'type' override)
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

        @Override
        public void type(String contentType) {
            // Avoid Spark's internal servlet error
        }
    }

    @Test
    void logoutSuccess () throws DataAccessException {
        // Arrange
        String username = "alice";
        String token = authStorage.addToken(username);


        Request request = new SimpleRequest(token);
        SimpleResponse response = new SimpleResponse();

        // Act
        String result = logoutService.logout(request, response);

        // Debug
        System.out.println("Logout Success Debug:");
        System.out.println("- Token: " + token);
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertEquals(200, response.status(), "Expected HTTP 200 for successful logout");
        assertTrue(result.contains("logged out"), "Expected success message in response");
        assertFalse(authStorage.isValidToken(token), "Token should be removed from authStorage");
    }

    @Test
    void logoutInvalidToken() {
        // Arrange
        String token = "invalid-token";

        Request request = new SimpleRequest(token);
        SimpleResponse response = new SimpleResponse();

        // Act
        String result = logoutService.logout(request, response);

        // Debug
        System.out.println("Logout Failure Debug:");
        System.out.println("- Token: " + token);
        System.out.println("- Result: " + result);
        System.out.println("- Status: " + response.status());

        // Assert
        assertEquals(401, response.status(), "Expected HTTP 401 for invalid token");
        assertTrue(result.contains("unauthorized"), "Expected error message in response");
    }
}
