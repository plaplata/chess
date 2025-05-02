package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        PieceType type = this.getPieceType();

        switch (type) {
            case KING:
                // King moves
                break;
            case QUEEN:
                // Queen moves
                break;
            case BISHOP:
                // Bishop moves
                int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}}; // All four diagonal directions

                for (int[] direction : directions) {
                    int row = myPosition.getRow();
                    int col = myPosition.getColumn();

                    while (true) {
                        row += direction[0];
                        col += direction[1];

                        // Check if position is still on the board
                        if (row < 1 || row > 8 || col < 1 || col > 8) {
                            break;
                        }

                        ChessPosition newPosition = new ChessPosition(row, col);
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);

                        // If square is empty, add move
                        if (pieceAtPosition == null) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        // If square has opponent's piece, add move and stop
                        else if (pieceAtPosition.getTeamColor() != this.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                            break;
                        }
                        // If square has our own piece, stop without adding
                        else {
                            break;
                        }
                    }
                }
                break;
            case KNIGHT:
                // Knight moves
                break;
            case ROOK:
                // Rook moves
                break;
            case PAWN:
                // Pawn moves
                break;
        }

        return moves;
    }
}
