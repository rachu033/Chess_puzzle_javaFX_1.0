package code.chess.view;

import code.chess.controller.BoardController;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public class PromotionView extends HBox {

    public PromotionView(Pane pane, FieldView fieldView, Consumer<BoardController.Event> eventHandler, int CELL_SIZE) {
        super(10);

        Pane blocker = new Pane();
        blocker.setPrefSize(pane.getWidth(), pane.getHeight());
        blocker.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        blocker.setPickOnBounds(true);
        pane.getChildren().add(blocker);

        this.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-padding: 5; -fx-border-color: black; -fx-border-width: 1;");

        double boxX = fieldView.getLayoutX();
        double boxY = fieldView.getLayoutY();
        int col = fieldView.getX();

        if (col > 4) {
            boxX -= CELL_SIZE * (col - 4);
        }

        double boxWidth = 4 * CELL_SIZE;

        if (boxX < 0) boxX = 0;
        if (boxX + boxWidth > pane.getWidth()) boxX = pane.getWidth() - boxWidth;
        if (boxY < 0) boxY = 0;
        if (boxY + (double) CELL_SIZE > pane.getHeight()) boxY = pane.getHeight() - (double) CELL_SIZE;

        this.setLayoutX(boxX);
        this.setLayoutY(boxY);
        this.setPrefSize(boxWidth, CELL_SIZE);

        boolean isWhite = fieldView.getY() == 0;

        PieceView[] options = new PieceView[]{
                new PieceView(isWhite, "Q", CELL_SIZE),
                new PieceView(isWhite, "R", CELL_SIZE),
                new PieceView(isWhite, "B", CELL_SIZE),
                new PieceView(isWhite, "N", CELL_SIZE)
        };

        for (PieceView option : options) {
            option.downSize();
            option.setOnMouseClicked((_) -> {
                eventHandler.accept(new BoardController.ChooseEvent(option));
                pane.getChildren().removeAll(this, blocker);
            });
            this.getChildren().add(option);
        }

        pane.getChildren().add(this);
    }
}