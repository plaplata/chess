package service;

import dataAccess.AuthMemoryStorage;
import dataAccess.UserMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.UserLogin;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserLoginTest {

    private UserLogin userLogin;
    private UserMemoryStorage userStorage;
    private AuthMemoryStorage authStorage;
    private Request request;
    private Response response;

    @BeforeEach
    void setup() {
        userStorage = new UserMemoryStorage();
        authStorage = new AuthMemoryStorage();
        userLogin = new UserLogin(userStorage, authStorage);
        request = mock(Request.class);
        response = mock(Response.class);
    }

    @Test
    void loginUser_Success() {
        userStorage.addUser("pablo", "pass123", "pablo@email.com");

        String requestBody = """
            {
              "username": "pablo",
              "password": "pass123"
            }
        """;

        when(request.body()).thenReturn(requestBody);

        String result = userLogin.login(request, response);

        verify(response).status(200);
        assertTrue(result.contains("\"username\":\"pablo\""));
        assertTrue(result.contains("\"authToken\""));
    }

    @Test
    void loginUser_InvalidCredentials() {
        // No user added to simulate invalid credentials
        String requestBody = """
            {
              "username": "ghost",
              "password": "wrongpass"
            }
        """;

        when(request.body()).thenReturn(requestBody);

        String result = userLogin.login(request, response);

        verify(response).status(401);
        assertTrue(result.contains("unauthorized"));
    }
}
