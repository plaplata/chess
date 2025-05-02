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

    //Pawn promotion helper method
    private void addPawnMoveIfValid(ChessBoard board, ChessPosition start, ChessPosition end, int promotionRow, Collection<ChessMove> moves) {
        if (end.getRow() == promotionRow) {
            // Promotion moves
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        } else {
            // Regular move
            moves.add(new ChessMove(start, end, null));
        }
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
                // King moves (8 directions)
                //direction {vertical, horizontal}
                int[][] kingDirections = {
                        {1,0}, {1,1}, {0,1}, {-1,1},  // Up, up-right, right, down-right
                        {-1,0}, {-1,-1}, {0,-1}, {1,-1} // Down, down-left, left, up-left
                };

                for (int[] direction : kingDirections) {
                    int newRow = myPosition.getRow() + direction[0];
                    int newCol = myPosition.getColumn() + direction[1];

                    // Check if new position is on the board
                    if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                        ChessPosition newPosition = new ChessPosition(newRow, newCol);
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);

                        // Add move if empty or opponent's piece
                        if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
                break;
            case QUEEN:
                // Queen moves (combination of rook and bishop directions)
                int[][] queenDirections = {
                        // Rook directions (up down right left)
                        {1,0}, {-1,0}, {0,1}, {0,-1},
                        // Bishop directions (diagonal)
                        {1,1}, {1,-1}, {-1,1}, {-1,-1}
                };

                for (int[] direction : queenDirections) {
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
            case BISHOP:
                // Bishop moves (4 diagonal directions)
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
                // Rook moves (up, down, right, left)
                int[][] rookDirections = {
                        {1, 0},   // Up
                        {-1, 0},  // Down
                        {0, 1},   // Right
                        {0, -1}   // Left
                };

                for (int[] direction : rookDirections) {
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
            case PAWN:
                //Pawn moves
                //ternary operator. If the team color is white, set (dir/startrow/promotionrow) to first value, otherwise to the second value
                int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
                int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
                int promotionRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

                // Single move forward
                ChessPosition oneForward = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
                if (board.getPiece(oneForward) == null) {
                    addPawnMoveIfValid(board, myPosition, oneForward, promotionRow, moves);

                    // Double move from starting position
                    if (myPosition.getRow() == startRow) {
                        ChessPosition twoForward = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                        if (board.getPiece(twoForward) == null) {
                            moves.add(new ChessMove(myPosition, twoForward, null));
                        }
                    }
                }

                // Diagonal captures
                int[] captureCols = {myPosition.getColumn() - 1, myPosition.getColumn() + 1};
                for (int col : captureCols) {
                    if (col >= 1 && col <= 8) {
                        ChessPosition capturePos = new ChessPosition(myPosition.getRow() + direction, col);
                        ChessPiece target = board.getPiece(capturePos);
                        if (target != null && target.getTeamColor() != this.getTeamColor()) {
                            addPawnMoveIfValid(board, myPosition, capturePos, promotionRow, moves);
                        }
                        // En Passant logic should go here, later
                    }
                }
                break;
        }

        return moves;
    }
}
