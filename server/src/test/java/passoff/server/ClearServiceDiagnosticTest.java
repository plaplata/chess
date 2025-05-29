package passoff.server;

import dataaccess.SQLAuthStorage;
import dataaccess.SQLGameStorage;
import dataaccess.SQLUserStorage;
import org.junit.jupiter.api.Test;
import server.ClearService;
import spark.Request;
import spark.Response;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceDiagnosticTest {

    // Inner class to safely fake Spark's Response
    static class FakeResponse extends Response {
        private int statusCode = 200;

        @Override
        public void status(int statusCode) {
            this.statusCode = statusCode;
            System.out.println("[DEBUG] Set response status to: " + statusCode);
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    @Test
    public void clearServiceDiagnostic() {
        ClearService clearService = new ClearService(new SQLUserStorage(), new SQLAuthStorage(), new SQLGameStorage());
        Request dummyRequest = null;
        FakeResponse dummyResponse = new FakeResponse();

        try {
            System.out.println("[DEBUG] Invoking ClearService.clearAll...");
            String result = clearService.clearAll(dummyRequest, dummyResponse);
            System.out.println("[DEBUG] clearAll result: " + result);
            System.out.println("[DEBUG] Final response status: " + dummyResponse.getStatusCode());

            assertNotNull(result, "clearAll result should not be null");
            assertEquals(200, dummyResponse.getStatusCode(), "Expected response status to be 200");
        } catch (Exception e) {
            fail("[ERROR] Exception during ClearService.clearAll: " + e.getMessage());
        }
    }
}
