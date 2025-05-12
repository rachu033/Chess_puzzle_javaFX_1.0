package code.chess.view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FieldView extends StackPane {
    private final boolean isWhite;
    private final int x;
    private final int y;
    private PieceView pieceView;

    public FieldView(boolean isWhite, int x, int y, double size) {
        this.isWhite = isWhite;
        this.x = x;
        this.y = y;
        this.setPrefSize(size, size);
        Rectangle rectangle = new Rectangle(size, size);
        rectangle.setFill(isWhite ? Color.BEIGE : Color.BROWN);
        this.getChildren().add(rectangle);
    }

    public boolean isWhite() {
        return isWhite;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public PieceView getPieceView() {
        return pieceView;
    }

    public void setPieceView(PieceView pieceView) {
        this.pieceView = pieceView;
    }

    public static int getX(String position) {
        char column = position.charAt(0);
        return column - 'A';
    }

    public static int getY(String position) {
        char row;
        row = position.charAt(1);
        return 8 - (row - '0');
    }
}
