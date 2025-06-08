package client;

import com.google.gson.Gson;
import ui.EscapeSequences;

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

    public ListGamesResponse listGames(String authToken) throws IOException {
        var responseBody = makeRequestWithAuth("/game", "GET", null, authToken);
        return gson.fromJson(responseBody, ListGamesResponse.class);
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws IOException {
        if (!"WHITE".equalsIgnoreCase(playerColor) && !"BLACK".equalsIgnoreCase(playerColor)) {
            throw new IllegalArgumentException("Player color must be 'WHITE' or 'BLACK'");
        }

        var requestBody = gson.toJson(new JoinGameRequest(gameID, playerColor));
        //old - for debug
        //System.out.println("▶️ Sending join request as player: " + requestBody);
        System.out.println("▶️ Loading game");
        makeRequestWithAuth("/game", "PUT", requestBody, authToken);

        drawBoard(playerColor); // ✅ Print the board after joining
    }

    public void observeGame(String authToken, int gameID) throws IOException {
        var requestBody = gson.toJson(new JoinGameRequest(gameID, "OBSERVER"));
        System.out.println("👁️ Sending join request as observer: " + requestBody);
        makeRequestWithAuth("/game", "PUT", requestBody, authToken);

        drawBoard("WHITE"); // ✅ Print the board after observing
    }

    private void drawBoard(String perspective) {
        final String light = EscapeSequences.SET_BG_COLOR_LIGHT_PEACH;
        final String dark = EscapeSequences.SET_BG_COLOR_TEAL_GREEN;
        final String reset = EscapeSequences.RESET_FORMATTING;

        String[][] board = {
                {EscapeSequences.BLACK_ROOK, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_BISHOP, EscapeSequences.BLACK_KING,
                        EscapeSequences.BLACK_QUEEN, EscapeSequences.BLACK_BISHOP, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_ROOK},
                {EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN,
                        EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN,
                        EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN},
                {EscapeSequences.WHITE_ROOK, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_BISHOP, EscapeSequences.WHITE_KING,
                        EscapeSequences.WHITE_QUEEN, EscapeSequences.WHITE_BISHOP, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_ROOK}
        };

        //convoluted but works
        boolean isWhitePerspective = !"WHITE".equalsIgnoreCase(perspective);

        System.out.println();

        for (int displayRow = 0; displayRow < 8; displayRow++) {
            int boardRow = isWhitePerspective ? 7 - displayRow : displayRow;
            int rank = isWhitePerspective ? displayRow + 1 : 8 - displayRow;

            System.out.print(" " + rank + " ");

            for (int displayCol = 0; displayCol < 8; displayCol++) {
                int boardCol = isWhitePerspective ? displayCol : 7 - displayCol;

                boolean isLight = (boardRow + boardCol) % 2 != 0;
                String bg = isLight ? light : dark;

                System.out.print(bg + board[boardRow][boardCol] + reset);
            }
            System.out.println();
        }

        System.out.print("   ");
        for (int col = 0; col < 8; col++) {
            char file = (char) ('a' + (!isWhitePerspective ? col : 7 - col));
            System.out.print(" " + file + " ");
        }
        System.out.println("\n");
    }


    public CreateGameResponse createGame(String authToken, String gameName) throws IOException {
        var requestBody = gson.toJson(Map.of("gameName", gameName));
        var responseBody = makeRequestWithAuth("/game", "POST", requestBody, authToken);
        return gson.fromJson(responseBody, CreateGameResponse.class);
    }

    public void logout(String authToken) throws IOException {
        makeRequestWithAuth("/session", "DELETE", null, authToken);
    }

    private String makeRequest(String endpoint, String method, String body) throws IOException {
        return makeRequestWithAuth(endpoint, method, body, null);
    }

    private String makeRequestWithAuth(String endpoint, String method, String body, String authToken) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }
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
                // Extract message from JSON
                String errorJson = responseBuilder.toString();
                String userMessage = errorJson;

                try {
                    // Parse the JSON and extract the message
                    Map<?, ?> errorMap = new Gson().fromJson(errorJson, Map.class);
                    if (errorMap.containsKey("message")) {
                        userMessage = (String) errorMap.get("message");
                    }
                } catch (Exception e) {
                    // If parsing fails, keep raw response
                }

                throw new IOException(userMessage);
            }

            return responseBuilder.toString();
        }
    }
}
