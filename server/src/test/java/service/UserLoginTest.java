package service;

import dataaccess.AuthMemoryStorage;
import dataaccess.UserMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.UserLogin;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

public class UserLoginTest {

    private UserLogin userLoginService;
    private UserMemoryStorage userStorage;
    private AuthMemoryStorage authStorage;

    @BeforeEach
    void setup() {
        userStorage = new UserMemoryStorage();
        authStorage = new AuthMemoryStorage();
        userLoginService = new UserLogin(userStorage, authStorage);
    }

    static class SimpleRequest extends Request {
        private final String body;

        public SimpleRequest(String body) {
            this.body = body;
        }

        @Override
        public String body() {
            return body;
        }
    }

    static class SimpleResponse extends Response {
        private int status;
        private String type;

        @Override
        public void status(int statusCode) {
            this.status = statusCode;
        }

        @Override
        public int status() {
            return status;
        }

        @Override
        public void type(String contentType) {
            this.type = contentType;
        }

        @Override
        public String type() {
            return type;
        }
    }

    @Test
    void loginSuccess() {
        // Arrange
        String username = "pablo";
        String password = "secure";
        userStorage.addUser(username, password, "pablo@email.com");

        String requestBody = """
            {
              "username": "pablo",
              "password": "secure"
            }
        """;

        Request request = new SimpleRequest(requestBody);
        SimpleResponse response = new SimpleResponse();

        // Act
        String result = userLoginService.login(request, response);

        // Assert
        System.out.println("Login result: " + result);
        assertEquals(200, response.status(), "Expected HTTP 200 for successful login");
        assertTrue(result.contains("\"username\":\"pablo\""), "Expected username in response");
        assertTrue(result.contains("authToken"), "Expected authToken in response");
    }

    @Test
    void loginFailInvalidCredentials() {
        // Arrange
        userStorage.addUser("alice", "password123", "alice@email.com");

        String requestBody = """
            {
              "username": "alice",
              "password": "wrongpassword"
            }
        """;

        Request request = new SimpleRequest(requestBody);
        SimpleResponse response = new SimpleResponse();

        // Act
        String result = userLoginService.login(request, response);

        // Assert
        System.out.println("Login failure result: " + result);
        assertEquals(401, response.status(), "Expected HTTP 401 for invalid login");
        assertTrue(result.contains("unauthorized"), "Expected error message in response");
    }
}
