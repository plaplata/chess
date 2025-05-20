package service;

import com.google.gson.Gson;
import dataAccess.AuthMemoryStorage;
import dataAccess.UserMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.UserReg;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserRegTest {

    private UserMemoryStorage userStorage;
    private AuthMemoryStorage authStorage;
    private UserReg userService;
    private Gson gson;

    @BeforeEach
    void setup() {
        userStorage = new UserMemoryStorage();
        authStorage = new AuthMemoryStorage();
        userService = new UserReg(userStorage, authStorage);
        gson = new Gson();
    }

    @Test
    void registerUser_Success() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        String requestBody = """
            {
              "username": "pablo",
              "password": "pass123",
              "email": "pablo@email.com"
            }
        """;
        when(request.body()).thenReturn(requestBody);

        String result = userService.register(request, response);

        verify(response).status(200);
        assertTrue(result.contains("\"username\":\"pablo\""));
        assertTrue(result.contains("\"authToken\""));
    }

    @Test
    void registerUser_AlreadyExists() {
        // Pre-populate the user store
        userStorage.addUser("pablo", "pass123", "pablo@email.com");

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        String requestBody = """
            {
              "username": "pablo",
              "password": "pass123",
              "email": "pablo@email.com"
            }
        """;
        when(request.body()).thenReturn(requestBody);

        String result = userService.register(request, response);

        verify(response).status(403);
        assertTrue(result.contains("already taken"));
    }
}
