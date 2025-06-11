package server.websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/connect")
public class WebSocketHandler {

    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("✅ Client connected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("📩 Received message: " + message);
        // TODO: Handle message dispatch (Milestone 8+)
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("🔌 Client disconnected: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("🚨 WebSocket error: " + error.getMessage());
    }
}
