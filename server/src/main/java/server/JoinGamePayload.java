package server;

import com.google.gson.annotations.SerializedName;

public class JoinGamePayload {
    public Double gameID;

    @SerializedName("playerColor")
    public String playerColor;

    // Add this helper method to detect if the field was missing
    public boolean hasPlayerColor() {
        return this.playerColor != null;
    }
}
