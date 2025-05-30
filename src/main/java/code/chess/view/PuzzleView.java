package code.chess.view;

import code.chess.controller.ApplicationController.SetFavouriteEvent;
import code.chess.controller.ApplicationController.Event;
import code.chess.model.puzzle.Puzzle;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.Objects;
import java.util.function.Consumer;

public class PuzzleView extends HBox {
    private final Puzzle puzzle;
    private Rectangle colorBlock;
    private Label turnLabel;
    private Consumer<Event> eventHandler;

    public PuzzleView(Puzzle puzzle) {
        super(10);
        this.puzzle = puzzle;
        initializeTile();
    }

    public void setEventHandler(Consumer<Event> handler) {
        this.eventHandler = handler;
    }

    private void initializeTile() {
        colorBlock = createColorBlock();
        Label ratingLabel = createRatingLabel();
        turnLabel = createTurnLabel();
        StackPane favouriteStar = createFavouriteStar();

        String backgroundColor = getTurnBackgroundColor();
        this.setStyle(createTileStyle(backgroundColor));

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(ratingLabel, turnLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(colorBlock, infoBox, favouriteStar);
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
        turnNumber = turnNumber / 2;

        Label turnLabel = new Label("Tura: " + turnNumber);
        turnLabel.setFont(new Font("Roboto", 14));
        turnLabel.setStyle("-fx-text-fill: " + (isWhiteToMove ? "#000000" : "#ffffff") + ";");
        return turnLabel;
    }

    private StackPane createFavouriteStar() {
        String imageFolderPath = "/code/chess/images/";
        String truePath = imageFolderPath + "favouriteTrue.png";
        String falsePath = imageFolderPath + "favouriteFalse.png";

        Image trueImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(truePath)));
        Image falseImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(falsePath)));

        ImageView starImageView = new ImageView(puzzle.isFavourite() ? trueImage : falseImage);
        starImageView.setFitWidth(36);
        starImageView.setFitHeight(36);
        starImageView.setSmooth(true);

        StackPane clickableArea = new StackPane(starImageView);
        clickableArea.setMinSize(36, 36);
        clickableArea.setStyle("-fx-cursor: hand;");

        clickableArea.setOnMouseClicked((MouseEvent event) -> {
            event.consume();
            puzzle.setFavourite(!puzzle.isFavourite());
            starImageView.setImage(puzzle.isFavourite() ? trueImage : falseImage);
            if (eventHandler != null) {
                eventHandler.accept(new SetFavouriteEvent(puzzle));
            }
        });

        return clickableArea;
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
        turnNumber = turnNumber / 2;

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
