package client.websocket;

import javax.websocket.*;
import java.net.URI;

@ClientEndpoint
public class ClientCommunicator {

    private Session session;
    private String serverUri = "ws://localhost:8080/connect";

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(serverUri));
        } catch (Exception e) {
            System.err.println("WebSocket connection failed: " + e.getMessage());
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
