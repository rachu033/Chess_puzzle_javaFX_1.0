package code.chess.model.chessgame.figure;

import code.chess.model.chessgame.Field;

public class King extends Piece {
    private boolean isCastlePossible;

    public King(boolean isWhite, Field field){
        super(isWhite, field);
        isCastlePossible = true;
    }

    @Override
    public boolean isValidMove(int targetCol, int targetRow) {
        if(isCaptured()) return false;
        int currentCol = getField().getX();
        int currentRow = getField().getY();

        int rowDiff = Math.abs(targetRow - currentRow);
        int colDiff = Math.abs(targetCol - currentCol);

        return (rowDiff <= 1) && (colDiff <= 1) && (rowDiff + colDiff > 0);
    }

    public boolean isCastlePossible() {
        return isCastlePossible;
    }

    public void setCastlePossible(boolean castlePossible) {
        this.isCastlePossible = castlePossible;
    }
}
