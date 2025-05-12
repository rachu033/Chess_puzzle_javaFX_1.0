package code.chess.model.chessgame;

import code.chess.model.chessgame.figure.Piece;

public class Field {
    private final boolean isWhite;
    private final int x;
    private final int y;
    private Piece piece;

    public Field(boolean isWhite, int x, int y) {
        this.isWhite = isWhite;
        this.x = x;
        this.y = y;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getAlgebraicNotation() {
        return getAlgebraicNotation(x, y);
    }

    public static String getAlgebraicNotation(int x, int y) {
        char column = (char) ('a' + x);
        int row = 8 - y;
        return "" + column + row;
    }

    public static int getX(String position) {
        char column = position.charAt(0);
        return column - 'a';
    }

    public static int getY(String position) {
        char row = position.charAt(1);
        return 8 - (row - '0');
    }
}
