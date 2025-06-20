package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;
    private ChessPosition enPassantTarget;

    public ChessPosition getEnPassantTarget() { return enPassantTarget; }
    public void setEnPassantTarget(ChessPosition pos) { this.enPassantTarget = pos; }

    public boolean isGameOver() {
        // Return false by default; replace with actual logic if needed
        return false;
    }

    public TeamColor getWinner() {
        // Return null by default; replace with actual logic if needed
        return null;
    }

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE; // White plays first
        this.board = new ChessBoard();
        this.board.setGame(this);
        this.board.resetBoard();
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK,
        OBSERVER
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        // Associate this game with the piece so it can access en passant info
        piece.setGame(this);

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        // CASTLING CODE
        if (piece.getPieceType() == ChessPiece.PieceType.KING && !piece.hasMoved()) {
            // Kingside castle
            if (canCastle(startPosition, 1)) {
                validMoves.add(new ChessMove(startPosition,
                        new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2), null));
            }
            // Queenside castle
            if (canCastle(startPosition, -1)) {
                validMoves.add(new ChessMove(startPosition,
                        new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2), null));
            }
        } // CASTILNG CODE END

        for (ChessMove move : potentialMoves) {
            ChessBoard testBoard = new ChessBoard();
            copyBoard(this.board, testBoard);

            ChessPiece movingPiece = testBoard.getPiece(move.getStartPosition());
            testBoard.addPiece(move.getStartPosition(), null);

            boolean isEnPassant = false;
            // Handle En Passant capture simulation
            if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
                    testBoard.getPiece(move.getEndPosition()) == null) {

                ChessPosition enPassantTarget = this.enPassantTarget;
                if (enPassantTarget != null &&
                        move.getEndPosition().equals(enPassantTarget)) {
                    isEnPassant = true;

                    int capturedPawnRow = move.getStartPosition().getRow();
                    ChessPosition capturedPawnPos = new ChessPosition(capturedPawnRow, move.getEndPosition().getColumn());

                    testBoard.addPiece(capturedPawnPos, null); // Remove the captured pawn
                }
            }

            testBoard.addPiece(move.getEndPosition(), movingPiece);

            // Handle promotion
            if (move.getPromotionPiece() != null) {
                testBoard.addPiece(move.getEndPosition(),
                        new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
            }

            ChessGame testGame = new ChessGame();
            testGame.setBoard(testBoard);
            testGame.setTeamTurn(piece.getTeamColor());

            if (!testGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        System.out.println("[DEBUG] Calculating valid moves for: " + startPosition);
        return validMoves;
    }

    // Pawn helper methods for makeMove: validatePawnPromotion, validateEnPassantDiagonalCapture
    private void validatePawnPromotion(ChessMove move, ChessPiece piece) throws InvalidMoveException {
        int promotionRow = (teamTurn == TeamColor.WHITE) ? 8 : 1;
        if (move.getEndPosition().getRow() == promotionRow) {
            ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
            if (promotionPiece == null || promotionPiece == ChessPiece.PieceType.PAWN || promotionPiece == ChessPiece.PieceType.KING) {
                throw new InvalidMoveException("Invalid pawn promotion");
            }
        }
    }
    private void validateEnPassantDiagonalCapture(ChessMove move, ChessPiece targetPiece) throws InvalidMoveException {
        if (targetPiece != null){
            return;
        }

        ChessPosition enPassantTarget = board.getGame().getEnPassantTarget();
        if (enPassantTarget == null || !move.getEndPosition().equals(enPassantTarget)) {
            throw new InvalidMoveException("Pawns can only move diagonally to capture");
        }
    }
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null){
            throw new InvalidMoveException("No piece at start position");
        }
        if (piece.getTeamColor() != teamTurn){
            throw new InvalidMoveException("Not your turn");
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2) {
            executeCastle(move);
            return;
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(move.getStartPosition().getRow() - move.getEndPosition().getRow()) == 2) {
            int enPassantRow = piece.getTeamColor() == TeamColor.WHITE
                    ? move.getStartPosition().getRow() + 1
                    : move.getStartPosition().getRow() - 1;
            enPassantTarget = new ChessPosition(enPassantRow, move.getStartPosition().getColumn());
            System.out.println("[DEBUG] En Passant target set at: " + enPassantTarget);
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        boolean isValidMove = validMoves.stream().anyMatch(valid ->
                valid.getEndPosition().equals(move.getEndPosition()) &&
                        Objects.equals(valid.getPromotionPiece(), move.getPromotionPiece()));

        if (!isValidMove){
            throw new InvalidMoveException("Invalid move for this piece");
        }

        if (piece.getPieceType() != ChessPiece.PieceType.KNIGHT &&
                isPathBlocked(move.getStartPosition(), move.getEndPosition())) {
            throw new InvalidMoveException("Path is blocked");
        }

        ChessPiece targetPiece = board.getPiece(move.getEndPosition());
        if (targetPiece != null && targetPiece.getTeamColor() == teamTurn) {
            throw new InvalidMoveException("Cannot capture your own piece");
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int promotionRow = (teamTurn == TeamColor.WHITE) ? 8 : 1;
            if (move.getEndPosition().getRow() == promotionRow) {
                validatePawnPromotion(move, piece);
            }

            if (move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) {
                validateEnPassantDiagonalCapture(move, targetPiece);
            }
        }

        ChessBoard testBoard = new ChessBoard();
        copyBoard(board, testBoard);
        testBoard.addPiece(move.getEndPosition(), piece);
        testBoard.addPiece(move.getStartPosition(), null);

        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        if (promotionPiece != null) {
            testBoard.addPiece(move.getEndPosition(), new ChessPiece(teamTurn, promotionPiece));
        }

        ChessGame testGame = new ChessGame();
        testGame.setBoard(testBoard);
        testGame.setTeamTurn(teamTurn);
        if (testGame.isInCheck(teamTurn)) {
            throw new InvalidMoveException("Move would leave king in check");
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
                board.getPiece(move.getEndPosition()) == null) {
            int capturedPawnRow = move.getStartPosition().getRow();
            ChessPosition capturedPawnPos = new ChessPosition(capturedPawnRow, move.getEndPosition().getColumn());
            board.addPiece(capturedPawnPos, null);
        }

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        piece.setMoved(true);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(teamTurn, move.getPromotionPiece()));
        }

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        if (!(piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(move.getStartPosition().getRow() - move.getEndPosition().getRow()) == 2)) {
            enPassantTarget = null;
        }
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
    // CASTLING HELPER METHODS START HERE
    private boolean canCastle(ChessPosition kingPos, int direction) {
        ChessPiece king = board.getPiece(kingPos);
        if (king == null || king.hasMoved() || king.getPieceType() != ChessPiece.PieceType.KING ||
                isInCheck(king.getTeamColor())) {
            return false;
        }

        int rookCol = direction > 0 ? 8 : 1;
        ChessPosition rookPos = new ChessPosition(kingPos.getRow(), rookCol);
        ChessPiece rook = board.getPiece(rookPos);
        if (rook == null || rook.hasMoved() || rook.getPieceType() != ChessPiece.PieceType.ROOK ||
                rook.getTeamColor() != king.getTeamColor()) {
            return false;
        }
        // Check path is clear
        int step = direction > 0 ? 1 : -1;
        for (int col = kingPos.getColumn() + step; col != rookCol; col += step) {
            if (board.getPiece(new ChessPosition(kingPos.getRow(), col)) != null) {
                return false;
            }
        }
        // Check squares king moves through aren't under attack
        for (int col = kingPos.getColumn(); col != kingPos.getColumn() + 2 * step + step; col += step) {
            if (isSquareUnderAttack(new ChessPosition(kingPos.getRow(), col), king.getTeamColor())) {
                return false;
            }
        }
        return true;
    }

    private boolean isSquareUnderAttack(ChessPosition position, TeamColor teamColor) {
        TeamColor opponentColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null || piece.getTeamColor() != opponentColor) {
                    continue;
                }

                Collection<ChessMove> moves = piece.pieceMoves(board, pos);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void executeCastle(ChessMove move) throws InvalidMoveException {
        ChessPiece king = board.getPiece(move.getStartPosition());
        if (king == null || king.getPieceType() != ChessPiece.PieceType.KING) {
            throw new InvalidMoveException("Invalid castling attempt");
        }
        int direction = move.getEndPosition().getColumn() > move.getStartPosition().getColumn() ? 1 : -1;
        int rookCol = direction > 0 ? 8 : 1;
        ChessPosition rookPos = new ChessPosition(move.getStartPosition().getRow(), rookCol);
        ChessPiece rook = board.getPiece(rookPos);
        // Move king
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), king);
        king.setMoved(true);
        // Move rook
        int newRookCol = move.getEndPosition().getColumn() - direction;
        ChessPosition newRookPos = new ChessPosition(move.getEndPosition().getRow(), newRookCol);
        board.addPiece(rookPos, null);
        board.addPiece(newRookPos, rook);
        rook.setMoved(true);
        // Switch turns
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    } // CASTLING HELPER METHODS END HERE

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

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            return false;
        }

        TeamColor opponentColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null || piece.getTeamColor() != opponentColor) {
                    continue;
                }

                Collection<ChessMove> moves = piece.pieceMoves(board, position);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

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
        return true; // No moves get out of check
    }


    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // Can't be stalemate if in check
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

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
        return true; // No valid moves found
    }


    public void setBoard(ChessBoard board) {
        this.board = board;
        this.board.setGame(this);
    }

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
