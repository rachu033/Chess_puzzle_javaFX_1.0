package code.chess.model.chessgame;

import code.chess.controller.BoardController;
import code.chess.model.chessgame.figure.*;

import java.util.function.Consumer;

public class LogicChess {
    private boolean isWhiteTure;
    private final Board board;
    private Consumer<BoardController.Event> eventHandler;

    public LogicChess() {
        this.board = new Board();
        isWhiteTure = true;
    }

    public void setEventHandler(Consumer<BoardController.Event> handler) {
        this.eventHandler = handler;
    }

    public String getLegalMoves(int currentCol, int currentRow) {
        StringBuilder legalMoves = new StringBuilder();
        Piece piece = getBoard().getPieceOnField(currentCol, currentRow);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(getBoard().getPieceOnField(i, j) != null && getBoard().getPieceOnField(i, j).isWhite() == piece.isWhite()) continue;
                if(!piece.isValidMove(i, j)) continue;
                if(piece instanceof Pawn) {
                    if(!isPathToFieldForPawn(currentCol, currentRow, i, j)) continue;
                }
                else {
                    if(!isPathToField(currentCol, currentRow, i, j) && !(piece instanceof Knight)) continue;
                }
                if(!isMoveSafe(piece, i, j)) continue;
                legalMoves.append(Field.getAlgebraicNotation(i, j)).append(" ");
            }
        }
        if(piece instanceof King king && king.isCastlePossible()) {
            Rook rook;
            if(board.getPieceOnField(0, king.getField().getY()) != null) {
                rook = (Rook) board.getPieceOnField(0, king.getField().getY());
                if(rook != null && rook.isCastlePossible()) {
                    if (canCastle(king, rook)) {
                        legalMoves.append(Field.getAlgebraicNotation(rook.getField().getX(), rook.getField().getY())).append("0");
                    }
                }
            }
            if(board.getPieceOnField(7, king.getField().getY()) != null) {
                rook = (Rook) board.getPieceOnField(7, king.getField().getY());
                if(rook != null && rook.isCastlePossible()) {
                    if (canCastle(king, rook)) {
                        legalMoves.append(Field.getAlgebraicNotation(rook.getField().getX(), rook.getField().getY())).append("0");
                    }
                }
            }
        }
        if(piece instanceof Pawn pawn) {
            Piece[] pieces = !isWhiteTure ? board.getWhitePieces() : board.getBlackPieces();
            for(Piece checkedPiece : pieces) {
                if(checkedPiece instanceof Pawn checkedPawn) {
                    if(checkedPawn.isEnPassantPossible()) {
                        if(pawn.getField().getY() == checkedPawn.getField().getY() && 1 == Math.abs(checkedPawn.getField().getX() - pawn.getField().getX())) {
                            legalMoves.append(Field.getAlgebraicNotation(checkedPawn.getField().getX(), checkedPawn.getField().getY())).append("*");
                            break;
                        }
                    }
                }
            }
        }
        return legalMoves.toString();
    }

    public boolean tryMove(int currentCol, int currentRow, int targetCol, int targetRow) {
        Piece piece = board.getPieceOnField(currentCol, currentRow);
        if(piece == null || piece.isWhite() != isWhiteTure) return false;
        //Castle
        if(piece instanceof King king && board.getPieceOnField(targetCol, targetRow) != null && board.getPieceOnField(targetCol, targetRow) instanceof Rook rook && piece.isWhite() == rook.isWhite()) {
            if (king.isCastlePossible() && rook.isCastlePossible() ) {
                if (canCastle(king, rook)) {
                    int rookCol = rook.getField().getX();
                    int rowY = rook.getField().getY();

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

        if(piece instanceof Pawn && (piece.getField().getY() == 0 || piece.getField().getY() == 7) && !(this instanceof LogicPuzzle)) {
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
            case "Q", "q" -> newPiece = new Queen(isWhite, null);
            case "R", "r" -> newPiece = new Rook(isWhite, null);
            case "B", "b" -> newPiece = new Bishop(isWhite, null);
            case "N", "n" -> newPiece = new Knight(isWhite, null);
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

    public boolean canCastle(King king, Rook rook) {
        int rookCol = rook.getField().getX();
        int rowY = rook.getField().getY();
        return isPathToField(rookCol, rowY, 4, rowY) &&
                !isFieldAttacked(rookCol == 0 ? 2 : 6, rowY, rook.isWhite()) &&
                !isFieldAttacked(rookCol == 0 ? 3 : 5, rowY, rook.isWhite()) &&
                !isFieldAttacked(4, rowY, king.isWhite());
    }

    public void afterMove() {
        isWhiteTure = !isWhiteTure;
        for (Piece piece : isWhiteTure ? board.getWhitePieces() : board.getBlackPieces()) {
            if (piece instanceof Pawn) {
                ((Pawn) piece).setEnPassantPossible(false);
            }
        }
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

    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTure() {
        return isWhiteTure;
    }

    Consumer<BoardController.Event> getEventHandler() {
        return eventHandler;
    }
}
