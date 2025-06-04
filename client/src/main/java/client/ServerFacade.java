package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String host, int port) {
        this.serverUrl = "http://" + host + ":" + port;
    }

    public AuthResponse register(String username, String password, String email) throws IOException {
        var requestBody = gson.toJson(Map.of(
                "username", username,
                "password", password,
                "email", email
        ));

        var responseJson = makeRequest("/user", "POST", requestBody);
        return gson.fromJson(responseJson, AuthResponse.class);
    }

    public AuthResponse login(String username, String password) throws IOException {
        var requestBody = gson.toJson(Map.of(
                "username", username,
                "password", password
        ));

        var responseJson = makeRequest("/session", "POST", requestBody);
        return gson.fromJson(responseJson, AuthResponse.class);
    }

    public void logout(String authToken) throws IOException {
        makeRequestWithAuth("/session", "DELETE", null, authToken);
    }

    private String makeRequestWithAuth(String endpoint, String method, String body, String authToken) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authToken);
        connection.setDoOutput(true);

        if (body != null) {
            try (var os = connection.getOutputStream()) {
                os.write(body.getBytes());
            }
        }

        var responseCode = connection.getResponseCode();
        InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (var reader = new BufferedReader(new InputStreamReader(responseStream))) {
            var responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            if (responseCode >= 400) {
                throw new IOException("Server returned error: " + responseBuilder);
            }

            return responseBuilder.toString();
        }
    }


    private String makeRequest(String endpoint, String method, String body) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (var os = connection.getOutputStream()) {
            os.write(body.getBytes());
        }

        var responseCode = connection.getResponseCode();
        InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (var reader = new BufferedReader(new InputStreamReader(responseStream))) {
            var responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            if (responseCode >= 400) {
                throw new IOException("Server returned error: " + responseBuilder);
            }

            return responseBuilder.toString();
        }
    }
}
