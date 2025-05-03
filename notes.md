This file is proof of a push

Phase 0:

I'm told to start by fixing all things necessary to
pass BishopMoveTests.

Implemented code for:
squares on ChessBoard

ChessPosition: row, col

ChessMove: startPosition, endPosition, promotionPiece

addPiece, getRow, getColumn, getPiece

pieceMoves: Empty ArrayList for now

---
ChessBoard: getPiece, addPiece altered (-1)

ChessMove: @Override: toString, equals, hashCode added

ChessPosition: @Override: toString, equals, hashCode added

ChessPiece: pieceColor, type. Implementation of bishop moves, switch

---
King moves added
Rook moves added
Queen moves added
Pawn moves added, En Passant logic not implemented, needs game state tracking
In ChessPiece, added addPawnMoveIfValid helper method for Pawn promotion logic
Knight moves added