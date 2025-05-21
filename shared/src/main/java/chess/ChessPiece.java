package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private ChessGame game;
    private boolean hasMoved = false;

    // hasMoved setMoved added for castling
    public boolean hasMoved() {
        return hasMoved;
    }
    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }

    public ChessGame getGame() {
        return this.game;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }
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

    //Helper method for King, Knight
    private void stepMove(ChessPosition myPosition, ChessBoard board, Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int row = myPosition.getRow() + direction[0];
            int col = myPosition.getColumn() + direction[1];

            if (row < 1 || row > 8 || col < 1 || col > 8) {
                continue; // skip if outside board
            }

            ChessPosition newPosition = new ChessPosition(row, col);
            ChessPiece pieceAtPosition = board.getPiece(newPosition);

            if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }

    //Helper method for Queen, Bishop and Rook
    private void slidingMove(ChessPosition myPosition, ChessBoard board, Collection<ChessMove> moves, int[][] directions) {
        for (int[] direction : directions) {
            int row = myPosition.getRow() + direction[0];
            int col = myPosition.getColumn() + direction[1];

            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece pieceAtPosition = board.getPiece(newPosition);

                if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }

                if (pieceAtPosition != null) {
                    break;
                }

                row += direction[0];
                col += direction[1];
            }
        }
    }

    //Helper method
    private void checkAndAddEnPassantMove(ChessBoard board, ChessPosition myPosition, int direction, int captureCol, Collection<ChessMove> moves) {
        ChessGame game = this.game;
        if (game == null){
            return;
        }

        ChessPosition enPassantTarget = game.getEnPassantTarget();
        if (enPassantTarget == null){
            return;
        }

        if (enPassantTarget.getRow() != myPosition.getRow() + direction || enPassantTarget.getColumn() != captureCol){
            return;
        }

        ChessPosition opponentPawnPos = new ChessPosition(myPosition.getRow(), captureCol);
        ChessPiece opponentPawn = board.getPiece(opponentPawnPos);

        if (opponentPawn != null &&
                opponentPawn.getPieceType() == PieceType.PAWN &&
                opponentPawn.getTeamColor() != this.getTeamColor()) {

            moves.add(new ChessMove(myPosition, enPassantTarget, null));
            System.out.println("[DEBUG] En Passant move generated: " + myPosition + " -> " + enPassantTarget);
        }
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece.PieceType type = this.type;

        switch(type){
            case KING:
                int[][] kingDirections = {
                        {1,-1},{1,0},{1,1},{0,-1},{0,1},{-1,-1},{-1,0},{-1,1}
                };
                stepMove(myPosition, board, moves, kingDirections);
                break;
            case QUEEN:
                int[][] queenDirections = {
                        {1,-1},{1,0},{1,1},{0,-1},{0,1},{-1,-1},{-1,0},{-1,1}
                };
                slidingMove(myPosition, board, moves, queenDirections);
                break;
            case BISHOP:
                int[][] bishopDirections = {
                        {1,-1},{1,1},{-1,-1},{-1,1}
                };
                slidingMove(myPosition, board, moves, bishopDirections);
                break;
            case ROOK:
                int[][] rookDirections = {
                        {0,-1},{0,1},{1,0},{-1,0}
                };
                slidingMove(myPosition, board, moves, rookDirections);
                break;
            case KNIGHT:
                int[][] knightDirections = {
                        {2,-1},{2,1},{1,-2},{1,2},{-1,-2},{-1,2},{-2,-1},{-2,1}
                };
                stepMove(myPosition, board, moves, knightDirections);
                break;
            case PAWN:
                int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
                int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
                int promotionRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

                // Single forward move
                ChessPosition oneForward = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
                if (oneForward.getRow() >= 1 && oneForward.getRow() <= 8 && board.getPiece(oneForward) == null) {
                    addPawnMoveIfValid(board, myPosition, oneForward, promotionRow, moves);

                    // Double move from start row
                    if (myPosition.getRow() == startRow) {
                        ChessPosition twoForward = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                        if (board.getPiece(twoForward) == null) {
                            moves.add(new ChessMove(myPosition, twoForward, null));
                        }
                    }
                }

                // Diagonal captures and En Passant
                for (int dCol = -1; dCol <= 1; dCol += 2) {
                    int captureCol = myPosition.getColumn() + dCol;
                    if (captureCol < 1 || captureCol > 8){
                        continue;
                    }

                    ChessPosition diagonal = new ChessPosition(myPosition.getRow() + direction, captureCol);
                    ChessPiece target = board.getPiece(diagonal);

                    if (target != null && target.getTeamColor() != this.getTeamColor()) {
                        addPawnMoveIfValid(board, myPosition, diagonal, promotionRow, moves);
                    }

                    checkAndAddEnPassantMove(board, myPosition, direction, captureCol, moves);
                }
                break;
        }
        return moves;
    }

    //left click, generated equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }
    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

}
