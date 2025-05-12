package code.chess.model.chessgame.figure;

import code.chess.model.chessgame.Field;

public class Bishop extends Piece {

    public Bishop(boolean isWhite, Field field){
        super(isWhite, field);
    }

    @Override
    public boolean isValidMove(int targetCol, int targetRow) {
        if(isCaptured()) return false;
        int currentCol = getField().getX();
        int currentRow = getField().getY();

        return Math.abs(targetRow - currentRow) == Math.abs(targetCol - currentCol);
    }
}
