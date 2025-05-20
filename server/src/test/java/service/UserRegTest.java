package service;

import dataAccess.AuthMemoryStorage;
import dataAccess.UserMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.UserReg;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

class UserRegTest {

    private UserReg userService;
    private UserMemoryStorage userStorage;
    private AuthMemoryStorage authStorage;

    @BeforeEach
    void setup() {
        userStorage = new UserMemoryStorage();
        authStorage = new AuthMemoryStorage();
        userService = new UserReg(userStorage, authStorage);
    }

    @Test
    void registerUser_Success() {
        SimpleRequest request = new SimpleRequest("""
            {
              "username": "pablo",
              "password": "pass123",
              "email": "pablo@email.com"
            }
        """);
        SimpleResponse response = new SimpleResponse();

        String result = userService.register(request, response);

        assertEquals(200, response.status(), "Expected HTTP 200 for success");
        assertTrue(result.contains("\"username\":\"pablo\""));
        assertTrue(result.contains("\"authToken\""));
    }

    @Test
    void registerUser_AlreadyExists() {
        userStorage.addUser("pablo", "pass123", "pablo@email.com");

        SimpleRequest request = new SimpleRequest("""
            {
              "username": "pablo",
              "password": "pass123",
              "email": "pablo@email.com"
            }
        """);
        SimpleResponse response = new SimpleResponse();

        String result = userService.register(request, response);

        assertEquals(403, response.status(), "Expected HTTP 403 for duplicate registration");
        assertTrue(result.contains("already taken"));
    }

    // Basic in-memory Request stub
    static class SimpleRequest extends Request {
        private final String body;

        SimpleRequest(String body) {
            this.body = body;
        }

        @Override
        public String body() {
            return body;
        }
    }

    // Basic in-memory Response stub
    static class SimpleResponse extends Response {
        private int status = 0;
        private String contentType = null;

        @Override
        public void status(int statusCode) {
            this.status = statusCode;
        }

        @Override
        public int status() {
            return status;
        }

        @Override
        public void type(String type) {
            this.contentType = type;
        }

        @Override
        public String type() {
            return contentType;
        }
    }

}
