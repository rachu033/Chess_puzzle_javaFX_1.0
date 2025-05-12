package code.chess.model.chessgame.figure;

import code.chess.model.chessgame.Field;

public class Pawn extends Piece {
    private boolean enPassantPossible;

    public Pawn(boolean isWhite, Field field){
        super(isWhite, field);
        enPassantPossible = false;
    }

    @Override
    public boolean isValidMove(int targetCol, int targetRow) {
        if(isCaptured()) return false;
        int currentCol = getField().getX();
        int currentRow = getField().getY();

        int direction = isWhite() ? -1 : 1;
        int startRow = isWhite() ? 6 : 1;

        return (targetCol == currentCol && targetRow == currentRow + direction) || (targetCol == currentCol && currentRow == startRow && targetRow == currentRow + 2 * direction) || (Math.abs(targetCol - currentCol) == 1 && targetRow == currentRow + direction);
    }

    public boolean isValidAttack(int targetCol, int targetRow) {
        if(isCaptured()) return false;
        int currentCol = getField().getX();
        int currentRow = getField().getY();

        int direction = isWhite() ? -1 : 1;

        return Math.abs(targetCol - currentCol) == 1 && targetRow == currentRow + direction;
    }

    public boolean isEnPassantPossible() {
        return enPassantPossible;
    }

    public void setEnPassantPossible(boolean enPassantPossible) {
        this.enPassantPossible = enPassantPossible;
    }
}
