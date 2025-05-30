package code.chess.view;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class FieldView extends StackPane {
    private final boolean isWhite;
    private final int x;
    private final int y;
    private PieceView pieceView;
    private final Rectangle background;

    public FieldView(boolean isWhite, int x, int y, double size) {
        this.isWhite = isWhite;
        this.x = x;
        this.y = y;
        this.setPrefSize(size, size);

        background = new Rectangle(size, size);
        background.setFill(isWhite ? Color.BEIGE : Color.BROWN);
        this.getChildren().add(background);
    }

    public void showMoveHint(String color) {
        if (getChildren().stream().anyMatch(node ->
                node instanceof Circle || (node instanceof Rectangle && "hint".equals(node.getId())))) {
            return;
        }

        Paint paint;
        if (color.equalsIgnoreCase("green")) {
            paint = Color.GREEN;
        } else if (color.equalsIgnoreCase("orange")) {
            paint = Color.ORANGE;
        } else {
            paint = Color.RED;
        }

        if (pieceView != null) {
            Rectangle overlay = new Rectangle(this.getPrefWidth(), this.getPrefHeight());
            overlay.setFill(paint);
            overlay.setOpacity(0.4);
            overlay.setId("hint");

            this.getChildren().add(overlay);
        } else {
            Circle hintCircle = new Circle(this.getPrefHeight() / 6);
            hintCircle.setFill(Color.rgb(32, 32, 32));
            hintCircle.setOpacity(0.6);

            this.getChildren().add(hintCircle);
        }
    }

    public void clearMoveHint() {
        getChildren().removeIf(node ->
                node instanceof Circle ||
                        (node instanceof Rectangle && "hint".equals(node.getId()))
        );
    }

    public void updateColor(Color color) {
        background.setFill(color);
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
