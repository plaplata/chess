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
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(command, session);
                case MAKE_MOVE -> handleMakeMove(command, session);
                case RESIGN -> handleResign(command, session);
                default -> sendError(session, "Unknown command type.");
            }
        } catch (Exception e) {
            sendError(session, "Malformed command: " + e.getMessage());
        }
    }

    private void handleConnect(UserGameCommand command, Session session) {
        System.out.println("📡 Handling CONNECT for game " + command.getGameID());
        // TODO: Validate authToken, locate game, register connection
    }

    private void handleMakeMove(UserGameCommand command, Session session) {
        System.out.println("♞ Handling MAKE_MOVE: " + command.getMove());
        // TODO: Parse move string, validate turn, apply move to game
    }

    private void handleResign(UserGameCommand command, Session session) {
        System.out.println("🏳️ Handling RESIGN for game " + command.getGameID());
        // TODO: Update game state to mark resign, notify others
    }

    private void sendError(Session session, String errorMsg) {
        try {
            ServerMessage error = ServerMessage.error(errorMsg);
            session.getBasicRemote().sendText(gson.toJson(error));
        } catch (IOException e) {
            System.err.println("❌ Failed to send error message: " + e.getMessage());
        }
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
