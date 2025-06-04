package client;

public class JoinGameRequest {
    public int gameID;
    public String playerColor; // Can be null for observers

    public JoinGameRequest(int gameID, String playerColor) {
        this.gameID = gameID;
        this.playerColor = playerColor; // null if observing
    }
}