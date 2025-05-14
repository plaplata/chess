package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;
    public ChessGame() {
        this.teamTurn = TeamColor.WHITE; // White plays first
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // Check if there's actually a piece at the starting position
        // Returns null if no piece exists at the given position (per method contract)
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        // Get all potential moves for the piece
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        // Check each move to see if it would leave king in check
        for (ChessMove move : potentialMoves) {
            // Create a test board to simulate the move
            ChessBoard testBoard = new ChessBoard();
            copyBoard(this.board, testBoard);

            // Execute the move on the test board
            ChessPiece movingPiece = testBoard.getPiece(move.getStartPosition());
            testBoard.addPiece(move.getEndPosition(), movingPiece);
            testBoard.addPiece(move.getStartPosition(), null);

            // Handle promotion
            if (move.getPromotionPiece() != null) {
                testBoard.addPiece(move.getEndPosition(),
                        new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
            }

            // Check if king would be in check after this move
            ChessGame testGame = new ChessGame();
            testGame.setBoard(testBoard);
            testGame.setTeamTurn(piece.getTeamColor());

            if (!testGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Basic validation
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn");
        }

        // Check if the move is valid for this piece
        Collection<ChessMove> validMoves = piece.pieceMoves(board, move.getStartPosition());
        boolean isValidMove = false;
        for (ChessMove validMove : validMoves) {
            if (validMove.getEndPosition().equals(move.getEndPosition()) &&
                    (validMove.getPromotionPiece() == move.getPromotionPiece())) {
                isValidMove = true;
                break;
            }
        }
        if (!isValidMove) {
            throw new InvalidMoveException("Invalid move for this piece");
        }

        // Check for blocking pieces (except knights)
        if (piece.getPieceType() != ChessPiece.PieceType.KNIGHT) {
            if (isPathBlocked(move.getStartPosition(), move.getEndPosition())) {
                throw new InvalidMoveException("Path is blocked");
            }
        }

        // Check if capturing own piece
        ChessPiece targetPiece = board.getPiece(move.getEndPosition());
        if (targetPiece != null && targetPiece.getTeamColor() == teamTurn) {
            throw new InvalidMoveException("Cannot capture your own piece");
        }

        // Pawn specific rules
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            // Check pawn promotion
            int promotionRow = (teamTurn == TeamColor.WHITE) ? 8 : 1;
            if (move.getEndPosition().getRow() == promotionRow) {
                if (move.getPromotionPiece() == null ||
                        move.getPromotionPiece() == ChessPiece.PieceType.PAWN ||
                        move.getPromotionPiece() == ChessPiece.PieceType.KING) {
                    throw new InvalidMoveException("Invalid pawn promotion");
                }
            }

            // Check diagonal moves must be captures
            if (move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) {
                if (targetPiece == null) {
                    throw new InvalidMoveException("Pawns can only move diagonally to capture");
                }
            }
        }

        // Check if move would leave king in check
        ChessBoard testBoard = new ChessBoard();
        copyBoard(board, testBoard);  // Create a copy of the board

        // Make the move on the test board
        testBoard.addPiece(move.getEndPosition(), piece);
        testBoard.addPiece(move.getStartPosition(), null);

        // If promotion, update the piece
        if (move.getPromotionPiece() != null) {
            testBoard.addPiece(move.getEndPosition(),
                    new ChessPiece(teamTurn, move.getPromotionPiece()));
        }

        // Check if king is in check after move
        ChessGame testGame = new ChessGame();
        testGame.setBoard(testBoard);
        testGame.setTeamTurn(teamTurn);

        if (testGame.isInCheck(teamTurn)) {
            throw new InvalidMoveException("Move would leave king in check");
        }

        // Make the actual move
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        // Handle promotion
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(),
                    new ChessPiece(teamTurn, move.getPromotionPiece()));
        }

        // Switch turns
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    // Helper method to copy board state
    private void copyBoard(ChessBoard source, ChessBoard destination) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                destination.addPiece(pos, source.getPiece(pos));
            }
        }
    }

    // Helper method to check if path is blocked
    /**
     * Checks if there are any pieces blocking the path between two positions.
     * Works for straight lines (rooks/queens) and diagonals (bishops/queens).
     *
     * @param start The starting position of the piece
     * @param end The destination position
     * @return true if any squares between start and end (exclusive) are occupied
     */
    private boolean isPathBlocked(ChessPosition start, ChessPosition end) {
        int rowStep = Integer.compare(end.getRow(), start.getRow());
        int colStep = Integer.compare(end.getColumn(), start.getColumn());

        int currentRow = start.getRow() + rowStep;
        int currentCol = start.getColumn() + colStep;

        while (currentRow != end.getRow() || currentCol != end.getColumn()) {
            if (board.getPiece(new ChessPosition(currentRow, currentCol)) != null) {
                return true;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return false;
    }

    // Helper method to find king's position
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null &&
                        piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null; // shouldn't happen in valid game
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) return false; // shouldn't happen in valid game

        // Check if any opponent's piece can attack the king
        TeamColor opponentColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == opponentColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        // Check if any move can get out of check
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        // Try the move
                        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
                        board.addPiece(move.getEndPosition(), piece);
                        board.addPiece(move.getStartPosition(), null);

                        boolean stillInCheck = isInCheck(teamColor);

                        // Undo the move
                        board.addPiece(move.getStartPosition(), piece);
                        board.addPiece(move.getEndPosition(), capturedPiece);

                        if (!stillInCheck) {
                            return false; // Found a move that gets out of check
                        }
                    }
                }
            }
        }
        return true; // No moves get out of check
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // Can't be stalemate if in check
        }

        // Check if any valid move exists
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, position);
                    for (ChessMove move : moves) {
                        // Try the move to see if it would leave king in check
                        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
                        board.addPiece(move.getEndPosition(), piece);
                        board.addPiece(move.getStartPosition(), null);

                        boolean inCheckAfterMove = isInCheck(teamColor);

                        // Undo the move
                        board.addPiece(move.getStartPosition(), piece);
                        board.addPiece(move.getEndPosition(), capturedPiece);

                        if (!inCheckAfterMove) {
                            return false; // Found a valid move
                        }
                    }
                }
            }
        }
        return true; // No valid moves found
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChessGame chessGame = (ChessGame) o;

        if (teamTurn != chessGame.teamTurn) {
            return false;
        }
        return Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        int result = teamTurn != null ? teamTurn.hashCode() : 0;
        result = 31 * result + (board != null ? board.hashCode() : 0);
        return result;
    }
}
