package code.chess.model.chessgame.figure;

import code.chess.model.chessgame.Field;

public class Rook extends Piece {
    private boolean isCastlePossible;

    public Rook(boolean isWhite, Field field){
        super(isWhite, field);
        isCastlePossible = true;
    }

    @Override
    public boolean isValidMove(int targetCol, int targetRow) {
        if(isCaptured()) return false;
        int currentCol = getField().getX();
        int currentRow = getField().getY();

        return targetRow == currentRow || targetCol == currentCol;
    }

    public boolean isCastlePossible() {
        return isCastlePossible;
    }

    public void setCastlePossible(boolean castlePossible) {
        isCastlePossible = castlePossible;
    }
}