package code.chess.model.chessgame.figure;

import code.chess.model.chessgame.Field;

public abstract class Piece {
    private final boolean isWhite;
    private boolean isCaptured;
    private Field field;

    public Piece(boolean isWhite, Field field) {
        this.isWhite = isWhite;
        this.field = field;
        if(field != null) {
            this.field.setPiece(this);
        }
        isCaptured = false;
    }

    public abstract boolean isValidMove(int targetCol, int targetRow);

    public boolean isWhite() {
        return isWhite;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public boolean isCaptured() {
        return isCaptured;
    }

    public void setCaptured(boolean captured) {
        isCaptured = captured;
    }
}
