package client.websocket;


import chess.ChessGame;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class ClientCommunicator {

    private Session session;
    private final String serverUri = "ws://localhost:8081/connect";
    private final Gson gson = new Gson();

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(serverUri));
        } catch (Exception e) {
            System.err.println("WebSocket connection failed: " + e.getMessage());
        }
    }

    public void sendConnectCommand(String authToken, int gameID) {
        if (session != null && session.isOpen()) {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            String json = gson.toJson(command);
            try {
                session.getBasicRemote().sendText(json);
                System.out.println("📤 Sent CONNECT command: " + json);
            } catch (IOException e) {
                System.err.println("❌ Failed to send CONNECT command: " + e.getMessage());
            }
        } else {
            System.err.println("❌ Cannot send CONNECT — session is not open.");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("✅ WebSocket connection opened.");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("🔌 WebSocket closed: " + closeReason);
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("🚨 WebSocket error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("📩 Received message: " + message);

        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    System.out.println("♟️ Game state loaded.");
                    ChessGame game = serverMessage.getGame();
                    System.out.println(new client.ui.ChessBoardRenderer().render(game.getBoard(), game.getTeamTurn()));
                }
                case NOTIFICATION -> {
                    System.out.println("🔔 Notification: " + serverMessage.getMessage());
                }
                case ERROR -> {
                    System.out.println("❌ Error: " + serverMessage.getErrorMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse server message: " + e.getMessage());
        }
    }


    public void disconnect() {
        try {
            if (session != null) session.close();
        } catch (Exception e) {
            System.err.println("Error closing WebSocket: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public void sendMakeMoveCommand(String authToken, int gameID, String moveStr) {
        if (session != null && session.isOpen()) {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
            command.setMove(moveStr);
            String json = gson.toJson(command);
            try {
                session.getBasicRemote().sendText(json);
                System.out.println("📤 Sent MAKE_MOVE command: " + json);
            } catch (IOException e) {
                System.err.println("❌ Failed to send MAKE_MOVE command: " + e.getMessage());
            }
        } else {
            System.err.println("❌ Cannot send MAKE_MOVE — session not open.");
        }
    }

    public void sendResignCommand(String authToken, int gameID) {
        if (session != null && session.isOpen()) {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            String json = gson.toJson(command);
            try {
                session.getBasicRemote().sendText(json);
                System.out.println("📤 Sent RESIGN command: " + json);
            } catch (IOException e) {
                System.err.println("❌ Failed to send RESIGN command: " + e.getMessage());
            }
        } else {
            System.err.println("❌ Cannot send RESIGN — session not open.");
        }
    }


}
