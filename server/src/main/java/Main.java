import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        // ✅ Start WebSocket Server separately using Tyrus on port 8081
        org.glassfish.tyrus.server.Server websocketServer =
                new org.glassfish.tyrus.server.Server("localhost", 8081, "/", null, server.websocket.WebSocketHandler.class);
        try {
            websocketServer.start();
            System.out.println("✅ WebSocket server started at ws://localhost:8081/connect");
        } catch (Exception e) {
            System.err.println("❌ Failed to start WebSocket server:");
            e.printStackTrace();
        }

        // ✅ Start Spark-based HTTP server (port 8080)
        Server server = new Server();
        server.run(8080); // Leave WebSockets disabled in Spark
    }
}
