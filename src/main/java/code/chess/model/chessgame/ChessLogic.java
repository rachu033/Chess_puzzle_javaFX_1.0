package code.chess.model.chessgame;

import code.chess.controller.BoardController;
import code.chess.model.chessgame.figure.*;

import java.util.function.Consumer;

public class ChessLogic {
    private boolean isWhiteTure;
    private final Board board;
    private Consumer<BoardController.Event> eventHandler;

    public ChessLogic() {
        this.board = new Board();
        isWhiteTure = true;
    }

    public boolean tryMove(int currentCol, int currentRow, int targetCol, int targetRow) {
        Piece piece = board.getPieceOnField(currentCol, currentRow);
        if(piece == null || piece.isWhite() != isWhiteTure) return false;
        //Castle
        if(piece instanceof King king && board.getPieceOnField(targetCol, targetRow) != null && board.getPieceOnField(targetCol, targetRow) instanceof Rook rook && piece.isWhite() == rook.isWhite()) {
            if (king.isCastlePossible() &&  rook.isCastlePossible()) {
                int rookCol = rook.getField().getX();
                int rowY = rook.getField().getY();
                boolean canCastle = isPathToField(rookCol, rowY, 4, rowY) &&
                        !isFieldAttacked(rookCol == 0 ? 2 : 6, rowY, rook.isWhite()) &&
                        !isFieldAttacked(rookCol == 0 ? 3 : 5, rowY, rook.isWhite());
                if (canCastle) {
                    int kingTargetCol = rookCol == 0 ? 2 : 6;
                    int rookTargetCol = rookCol == 0 ? 3 : 5;

                    king.getField().setPiece(null);
                    rook.getField().setPiece(null);

                    king.setField(board.getField(kingTargetCol, rowY));
                    rook.setField(board.getField(rookTargetCol, rowY));

                    king.getField().setPiece(king);
                    rook.getField().setPiece(rook);

                    king.setCastlePossible(false);
                    rook.setCastlePossible(false);
                    afterMove();
                    return true;
                }
            }
        }
        // Valid move for piece
        if (!piece.isValidMove(targetCol, targetRow)) {
            return false;
        }

        // Pawn
        if(piece instanceof Pawn) {
            if(!isPathToFieldForPawn(currentCol, currentRow, targetCol, targetRow)) return false;
        }
        // Oder piece
        else {
            if(!isPathToField(currentCol, currentRow, targetCol, targetRow) && !(piece instanceof Knight)) return false;
        }

        // King attacked after move
        if(!isMoveSafe(piece, targetCol, targetRow)) {
            return false;
        }

        // Captured opponent or own pieces
        Piece target = board.getPieceOnField(targetCol, targetRow);
        if (target != null && target.isWhite() != piece.isWhite()) {
            target.setCaptured(true);
            target.setField(null);
            board.getField(targetCol, targetRow).setPiece(null);
        } else if (target != null) {
            return false;
        }

        // En passant
        if (piece instanceof Pawn) {
            if(Math.abs(targetRow - currentRow) == 2) ((Pawn) piece).setEnPassantPossible(true);
            Field oldField = piece.getField();
            // En Passant
            int oldCol = oldField.getX();
            int oldRow = oldField.getY();
            int direction = piece.isWhite() ? -1 : 1;
            if (Math.abs(targetCol - oldCol) == 1 && targetRow - oldRow == direction && board.getField(targetCol, targetRow).getPiece() == null) {
                Field capturedPawnField = board.getField(targetCol, oldRow);
                Piece captured = capturedPawnField.getPiece();
                if (captured instanceof Pawn pawn && captured.isWhite() != isWhiteTure && pawn.isEnPassantPossible()) {
                    captured.setCaptured(true);
                    captured.setField(null);
                    capturedPawnField.setPiece(null);
                }
            }
        }

        // First move king or rook
        if(piece instanceof King) ((King) piece).setCastlePossible(false);
        if(piece instanceof Rook) ((Rook) piece).setCastlePossible(false);

        piece.setField(board.getField(targetCol, targetRow));
        board.getField(currentCol, currentRow).setPiece(null);
        board.getField(targetCol, targetRow).setPiece(piece);

        if(piece instanceof Pawn && (piece.getField().getY() == 0 || piece.getField().getY() == 7) && !(this instanceof PuzzleLogic)) {
            eventHandler.accept(new BoardController.PromotionEvent(board.getField(targetCol, targetRow)));
        }

        afterMove();
        return true;
    }

    public void promotePawn(String name, boolean isWhite) {
        Piece[] pieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        Pawn pawn = null;
        for (Piece piece : pieces) {
            if (piece instanceof Pawn && !piece.isCaptured() && (piece.getField().getY() == 0 || piece.getField().getY() == 7)) {
                pawn = (Pawn) piece;
                break;
            }
        }
        Piece newPiece = null;
        switch (name) {
            case "Q" -> newPiece = new Queen(isWhite, null);
            case "R" -> newPiece = new Rook(isWhite, null);
            case "B" -> newPiece = new Bishop(isWhite, null);
            case "N" -> newPiece = new Knight(isWhite, null);
            case "q" -> newPiece = new Queen(isWhite, null);
            case "r" -> newPiece = new Rook(isWhite, null);
            case "b" -> newPiece = new Bishop(isWhite, null);
            case "n" -> newPiece = new Knight(isWhite, null);
        }

        if(pawn != null && newPiece != null) {
            Field field = pawn.getField();
            pawn.setField(null);
            field.setPiece(null);
            newPiece.setField(field);
            field.setPiece(newPiece);

            for (int i=0; i < pieces.length; i++) {
                if (pieces[i] == pawn) {
                    pieces[i] = newPiece;
                    break;
                }
            }
        }
    }

    public void afterMove() {
        isWhiteTure = !isWhiteTure;
        for (Piece piece : isWhiteTure ? board.getWhitePieces() : board.getBlackPieces()) {
            if (piece instanceof Pawn) {
                ((Pawn) piece).setEnPassantPossible(false);
            }
        }
    }

    public Board getBoard() {
        return board;
    }

    public void setEventHandler(Consumer<BoardController.Event> handler) {
        this.eventHandler = handler;
    }

    protected boolean isPathToField(int currentCol, int currentRow, int targetCol, int targetRow) {
        int colDiff = targetCol - currentCol;
        int rowDiff = targetRow - currentRow;

        if (!(colDiff == 0 || rowDiff == 0 || Math.abs(colDiff) == Math.abs(rowDiff))) {
            return false;
        }

        int colStep = Integer.compare(colDiff, 0);
        int rowStep = Integer.compare(rowDiff, 0);

        int col = currentCol + colStep;
        int row = currentRow + rowStep;

        while (col != targetCol || row != targetRow) {
            if (board.getPieceOnField(col, row) != null) {
                return false;
            }
            col += colStep;
            row += rowStep;
            if(col == targetCol && row == targetRow) break;
        }

        return true;
    }

    protected boolean isPathToFieldForPawn(int currentCol, int currentRow, int targetCol, int targetRow) {
        Piece movingPawn = board.getPieceOnField(currentCol, currentRow);
        if (!(movingPawn instanceof Pawn)) return false;

        boolean isWhite = movingPawn.isWhite();
        int direction = isWhite ? -1 : 1;
        int rowDiff = targetRow - currentRow;
        int colDiff = targetCol - currentCol;

        if (colDiff == 0 && rowDiff == direction) {
            return board.getPieceOnField(targetCol, targetRow) == null;
        }

        if (colDiff == 0 && rowDiff == 2 * direction) {
            if ((isWhite && currentRow == 6) || (!isWhite && currentRow == 1)) {
                return board.getPieceOnField(targetCol, currentRow + direction) == null && board.getPieceOnField(currentCol, currentRow + 2 * direction) == null;
            }
            return false;
        }

        if (Math.abs(colDiff) == 1 && rowDiff == direction) {
            Piece targetPiece = board.getPieceOnField(targetCol, targetRow);

            if (targetPiece != null && targetPiece.isWhite() != isWhite) {
                return true;
            }

            Field sideField = board.getField(targetCol, currentRow);
            Piece sidePiece = sideField.getPiece();
            if (sidePiece instanceof Pawn adjacentPawn && sidePiece.isWhite() != isWhite) {
                return adjacentPawn.isEnPassantPossible();
            }
            return false;
        }
        return false;
    }

    protected boolean isMoveSafe(Piece piece, int targetCol, int targetRow) {
        Field currentField = piece.getField();
        Field targetField = board.getField(targetCol, targetRow);
        Piece capturedPiece = board.getPieceOnField(targetCol, targetRow);

        currentField.setPiece(null);
        targetField.setPiece(piece);
        piece.setField(targetField);
        if(capturedPiece != null) capturedPiece.setCaptured(true);

        boolean isSafe = !isCheck(piece.isWhite());

        board.getField(targetCol, targetRow).setPiece(capturedPiece);
        piece.setField(currentField);
        currentField.setPiece(piece);
        if(capturedPiece != null) capturedPiece.setCaptured(false);

        return isSafe;
    }

    protected boolean isFieldAttacked(int col, int row, boolean isWhite) {
        Piece[] pieces = !isWhite ? board.getWhitePieces() : board.getBlackPieces();
        for (Piece piece : pieces) {
            if (!(piece instanceof Pawn) && piece.isValidMove(col, row)) {
                if(piece instanceof Knight) return true;
                if(isPathToField(piece.getField().getX(), piece.getField().getY(), col, row)){
                    return true;
                }
            }
            if (piece instanceof Pawn pawn && pawn.isValidAttack(col, row)) {
                if(isPathToFieldForPawn(piece.getField().getX(), piece.getField().getY(), col, row)){
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isCheck(boolean isWhite) {
        King king = null;
        Piece[] pieces = isWhite ? board.getWhitePieces() : board.getBlackPieces();
        for (Piece piece : pieces) {
            if (piece instanceof King) {
                king = (King) piece;
                break;
            }
        }
        if (king == null) return false;
        return isFieldAttacked(king.getField().getX(), king.getField().getY(), isWhite);
    }

    public boolean isWhiteTure() {
        return isWhiteTure;
    }

    Consumer<BoardController.Event> getEventHandler() {
        return eventHandler;
    }
}
