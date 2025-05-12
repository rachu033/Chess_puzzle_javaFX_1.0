package code.chess.model.chessgame.figure;

import code.chess.model.chessgame.Field;

public class Knight extends Piece {

    public Knight(boolean isWhite, Field field){
        super(isWhite, field);
    }

    @Override
    public boolean isValidMove(int targetCol, int targetRow) {
        if(isCaptured()) return false;
        int currentCol = getField().getX();
        int currentRow = getField().getY();

        int colDiff = Math.abs(targetCol - currentCol);
        int rowDiff = Math.abs(targetRow - currentRow);

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
}