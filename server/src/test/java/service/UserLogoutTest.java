package service;

import dataAccess.AuthMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.UserLogout;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserLogoutTest {

    private AuthMemoryStorage authStorage;
    private UserLogout logoutService;

    @BeforeEach
    void setup() {
        authStorage = new AuthMemoryStorage();
        logoutService = new UserLogout(authStorage);
    }

    @Test
    void logout_Success() {
        // Arrange
        String token = "valid-token";
        authStorage.addToken(token, "pablo");

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.headers("Authorization")).thenReturn(token);

        // Act
        String result = logoutService.logout(request, response);

        // Assert
        try {
            verify(response).status(200);
            verify(response).type("application/json");
        } catch (Throwable e) {
            fail("Expected status 200 and JSON response type, but verification failed.\n" + e.getMessage());
        }

        assertTrue(result.contains("logged out"),
                "Expected response to contain 'logged out', but got: " + result);
    }

    @Test
    void logout_InvalidToken() {
        // Arrange
        String token = "invalid-token"; // not added to authStorage

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.headers("Authorization")).thenReturn(token);

        // Act
        String result = logoutService.logout(request, response);

        // Assert
        try {
            verify(response).status(401);
        } catch (Throwable e) {
            fail("Expected status 401 for invalid token, but verification failed.\n" + e.getMessage());
        }

        assertTrue(result.contains("unauthorized"),
                "Expected response to contain 'unauthorized', but got: " + result);
    }
}
