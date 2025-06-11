package client.websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class ClientCommunicator {

    private Session session;
    private final String serverUri = "ws://localhost:8080/connect";
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
        // Stub for later handling of ServerMessage
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
}
