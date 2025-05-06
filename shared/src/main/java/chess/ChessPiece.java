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
        ChessPiece.PieceType type = this.type;

        switch(type){
            case KING:
                //directions 8
                int[][] kingDirections = {
                        {1,-1},{1,0},{1,1},{0,-1},{0,1},{-1,-1},{-1,0},{-1,1}
                };
                //For loop
                for(int[] direction : kingDirections){
                    //row col
                    int row = myPosition.getRow() + direction[0];
                    int col = myPosition.getColumn() + direction[1];

                    //boundary check
                    if(row < 1 || row > 8 || col < 1 || col > 8) continue;

                    //newPosition pieceAtPosition
                    ChessPosition newPosition = new ChessPosition(row,col);
                    ChessPiece pieceAtPosition = board.getPiece(newPosition);

                    //move allowed
                    if(pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()){
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
                break;
            case QUEEN:
                //directions 8
                int[][] queenDirections = {
                        {1,-1},{1,0},{1,1},{0,-1},{0,1},{-1,-1},{-1,0},{-1,1}
                };
                //For loop
                for(int[] direction : queenDirections){
                    //row col
                    int row = myPosition.getRow() + direction[0];
                    int col = myPosition.getColumn() + direction[1];

                    //while loop(boundary check)
                    while(row >= 1 && row <= 8 && col >= 1 && col <= 8){
                        //newPosition pieceAtPosition
                        ChessPosition newPosition = new ChessPosition(row,col);
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        //move allowed
                        if(pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()){
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        //Stop after any piece
                        if(pieceAtPosition != null) break;

                        //row col - keeps sliding
                        row += direction[0];
                        col += direction[1];

                    }
                }
                break;
            case BISHOP:
                //directions 4 - diagonals
                int[][] bishopDirections = {
                        {1,-1},{1,1},{-1,-1},{-1,1}
                };
                //For loop
                for(int[] direction : bishopDirections){
                    //row col
                    int row = myPosition.getRow() + direction[0];
                    int col = myPosition.getColumn() + direction[1];

                    //while loop(boundary check)
                    while(row >= 1 && row <= 8 && col >= 1 && col <= 8){
                        //newPosition pieceAtPosition
                        ChessPosition newPosition = new ChessPosition(row,col);
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        //move allowed
                        if(pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()){
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        //Stop after any piece
                        if(pieceAtPosition != null) break;

                        //row col - keeps sliding
                        row += direction[0];
                        col += direction[1];

                    }
                }
                break;
            case ROOK:
                //directions 4 - diagonals
                int[][] rookDirections = {
                        {0,-1},{0,1},{1,0},{-1,0}
                };
                //For loop
                for(int[] direction : rookDirections){
                    //row col
                    int row = myPosition.getRow() + direction[0];
                    int col = myPosition.getColumn() + direction[1];

                    //while loop(boundary check)
                    while(row >= 1 && row <= 8 && col >= 1 && col <= 8){
                        //newPosition pieceAtPosition
                        ChessPosition newPosition = new ChessPosition(row,col);
                        ChessPiece pieceAtPosition = board.getPiece(newPosition);
                        //move allowed
                        if(pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()){
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        //Stop after any piece
                        if(pieceAtPosition != null) break;

                        //row col - keeps sliding
                        row += direction[0];
                        col += direction[1];

                    }
                }
                break;
            case KNIGHT:
                //8 L shapes
                int[][] knightDirections = {
                        {2,-1},{2,1},{1,-2},{1,2},{-1,-2},{-1,2},{-2,-1},{-2,1}
                };
                //For loop
                for (int[] direction : knightDirections) {
                    //row col
                    int row = myPosition.getRow() + direction[0];
                    int col = myPosition.getColumn() + direction[1];

                    //boundary check
                    if (row < 1 || row > 8 || col < 1 || col > 8) continue;

                    //newPosition pieceAtPosition
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece pieceAtPosition = board.getPiece(newPosition);

                    //move allowed
                    if (pieceAtPosition == null || pieceAtPosition.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
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
