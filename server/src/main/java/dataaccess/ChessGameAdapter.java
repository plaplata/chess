package dataaccess;

import chess.ChessGame;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessGameAdapter implements JsonDeserializer<ChessGame>, JsonSerializer<ChessGame> {
    @Override
    public ChessGame deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return new Gson().fromJson(json, ChessGame.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize ChessGame, returning blank game.");
            return new ChessGame(); // Fallback
        }
    }

    @Override
    public JsonElement serialize(ChessGame src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject gameJson = new JsonObject();

        gameJson.addProperty("teamTurn", src.getTeamTurn().toString());
        //gameJson.add("board", context.serialize(src.getBoard()));
        gameJson.addProperty("gameOver", src.isGameOver());
        gameJson.add("winner", src.getWinner() != null ? new JsonPrimitive(src.getWinner().toString()) : JsonNull.INSTANCE);

        return gameJson;
    }

}
