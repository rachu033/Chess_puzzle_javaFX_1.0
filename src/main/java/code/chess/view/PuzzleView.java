package code.chess.view;

import code.chess.model.puzzle.Puzzle;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

public class PuzzleView extends HBox {
    private final Puzzle puzzle;
    private Rectangle colorBlock;
    private Label turnLabel;

    public PuzzleView(Puzzle puzzle) {
        super(10);
        this.puzzle = puzzle;
        initializeTile();
    }

    private void initializeTile() {
        colorBlock = createColorBlock();
        Label ratingLabel = createRatingLabel();
        turnLabel = createTurnLabel();

        String backgroundColor = getTurnBackgroundColor();
        this.setStyle(createTileStyle(backgroundColor));

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(ratingLabel, turnLabel);

        this.getChildren().addAll(colorBlock, infoBox);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        setPadding(new Insets(5));
    }

    private Rectangle createColorBlock() {
        Rectangle colorBlock = new Rectangle(10, 45, getColorByRating(puzzle.isSolved()));
        colorBlock.setArcWidth(10);
        colorBlock.setArcHeight(10);
        colorBlock.setStyle("-fx-padding: 5px;");
        return colorBlock;
    }

    private Label createRatingLabel() {
        Label ratingLabel = new Label("Ranking: " + puzzle.getRating());
        ratingLabel.setFont(new Font("Roboto", 14));
        ratingLabel.setStyle("-fx-text-fill: #777777;");
        return ratingLabel;
    }

    private Label createTurnLabel() {
        int moveCount = puzzle.getPgn().trim().isEmpty() ? 0 : puzzle.getPgn().trim().split("\\s+").length;
        int turnNumber = moveCount + 1;
        boolean isWhiteToMove = turnNumber % 2 != 0;
        turnNumber = turnNumber/2;

        Label turnLabel = new Label("Tura: " + turnNumber);
        turnLabel.setFont(new Font("Roboto", 14));
        turnLabel.setStyle("-fx-text-fill: " + (isWhiteToMove ? "#000000" : "#ffffff") + ";");
        return turnLabel;
    }

    private String createTileStyle(String backgroundColor) {
        return "-fx-background-color: " + backgroundColor + "; "
                + "-fx-background-radius: 0px; "
                + "-fx-border-radius: 0px; "
                + "-fx-border-color: transparent; "
                + "-fx-border-width: 2px;";
    }

    public void setSelected(boolean selected) {
        String backgroundColor = getTurnBackgroundColor();
        String borderColor = selected ? "orange" : "transparent";

        this.setStyle("-fx-background-color: " + backgroundColor + "; "
                + "-fx-background-radius: 0px; "
                + "-fx-border-radius: 0px; "
                + "-fx-border-color: " + borderColor + "; "
                + "-fx-border-width: 2px;");

        updateTurnLabelStyle();
    }

    private String getTurnBackgroundColor() {
        int moveCount = puzzle.getPgn().trim().isEmpty() ? 0 : puzzle.getPgn().trim().split("\\s+").length;
        boolean isWhiteToMove = (moveCount + 1) % 2 != 0;
        return isWhiteToMove ? "#DCDCDC" : "#2f2f2f";
    }

    private void updateTurnLabelStyle() {
        int moveCount = puzzle.getPgn().trim().isEmpty() ? 0 : puzzle.getPgn().trim().split("\\s+").length;
        int turnNumber = moveCount + 1;
        boolean isWhiteToMove = turnNumber % 2 != 0;
        turnNumber = turnNumber/2;

        turnLabel.setText("Tura: " + turnNumber);
        turnLabel.setStyle("-fx-text-fill: " + (isWhiteToMove ? "#000000" : "#ffffff") + ";");
    }

    private Color getColorByRating(boolean isSolved) {
        return isSolved ? Color.GREEN : Color.ORANGE;
    }

    public void updateSolvedColor() {
        colorBlock.setFill(getColorByRating(puzzle.isSolved()));
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }
}

