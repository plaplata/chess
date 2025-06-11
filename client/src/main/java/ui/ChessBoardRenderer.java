package client.ui;

import chess.*;

public class ChessBoardRenderer {

    public String render(ChessBoard board, ChessGame.TeamColor perspective) {
        StringBuilder sb = new StringBuilder();
        int startRow = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (perspective == ChessGame.TeamColor.WHITE) ? 0 : 9;
        int step = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        for (int row = startRow; row != endRow; row += step) {
            sb.append(row).append(" ");
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                sb.append(pieceSymbol(piece)).append(" ");
            }
            sb.append("\n");
        }

        sb.append("  a b c d e f g h\n");
        return sb.toString();
    }

    private String pieceSymbol(ChessPiece piece) {
        if (piece == null) return ".";
        String symbol = switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? symbol : symbol.toLowerCase();
    }
}
