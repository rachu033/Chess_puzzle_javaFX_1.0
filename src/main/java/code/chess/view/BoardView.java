package code.chess.view;

import code.chess.controller.BoardController;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

public class BoardView extends Pane {
    private final FieldView[][] fieldViews;
    private FieldView selectedField = null;
    private Consumer<BoardController.Event> eventHandler;
    private static int CELL_SIZE;
    private static int MARGIN;
    private static int GRID_SIZE;

    public BoardView(int CELL_SIZE, int MARGIN, int GRID_SIZE) {
        this.setPrefSize(GRID_SIZE * CELL_SIZE + 2 * MARGIN, GRID_SIZE * CELL_SIZE + 2 * MARGIN);
        this.setStyle("-fx-background-color: #3e2723;");
        BoardView.CELL_SIZE = CELL_SIZE;
        BoardView.MARGIN = MARGIN;
        BoardView.GRID_SIZE = GRID_SIZE;

        fieldViews = new FieldView[GRID_SIZE][GRID_SIZE];
        drawBoard();
    }

    public void drawBoard() {
        this.getChildren().clear();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                boolean isWhite = (row + col) % 2 == 0;
                FieldView fieldView = new FieldView(isWhite, col, row, CELL_SIZE);

                fieldView.setLayoutX(MARGIN + col * CELL_SIZE);
                fieldView.setLayoutY(MARGIN + row * CELL_SIZE);
                fieldView.setOnMousePressed(_ -> handleFieldPressed(fieldView));
                this.setOnMouseReleased(e -> {
                    int c = (int) ((e.getX() - MARGIN) / CELL_SIZE);
                    int r = (int) ((e.getY() - MARGIN) / CELL_SIZE);
                    if (c >= 0 && c < GRID_SIZE && r >= 0 && r < GRID_SIZE) {
                        handleFieldReleased(fieldViews[c][r]);
                    }
                });

                fieldViews[col][row] = fieldView;
                this.getChildren().add(fieldView);
            }
        }
        addCoordinates();
    }

    private void addCoordinates() {
        String letters = "abcdefgh";
        for (int i = 0; i < GRID_SIZE; i++) {
            Label top = createCoordLabel(String.valueOf(letters.charAt(i)));
            top.setLayoutX(MARGIN + i * CELL_SIZE + (double) CELL_SIZE / 2 - 5);
            top.setLayoutY(3);
            this.getChildren().add(top);

            Label bottom = createCoordLabel(String.valueOf(letters.charAt(i)));
            bottom.setLayoutX(MARGIN + i * CELL_SIZE + (double) CELL_SIZE / 2 - 5);
            bottom.setLayoutY(MARGIN + GRID_SIZE * CELL_SIZE);
            this.getChildren().add(bottom);

            Label left = createCoordLabel(String.valueOf(GRID_SIZE - i));
            left.setLayoutX(5);
            left.setLayoutY(MARGIN + i * CELL_SIZE + (double) CELL_SIZE / 2 - 10);
            this.getChildren().add(left);

            Label right = createCoordLabel(String.valueOf(GRID_SIZE - i));
            right.setLayoutX(MARGIN + GRID_SIZE * CELL_SIZE + 5);
            right.setLayoutY(MARGIN + i * CELL_SIZE + (double) CELL_SIZE / 2 - 10);
            this.getChildren().add(right);
        }
    }

    private Label createCoordLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }

    public void setEventHandler(Consumer<BoardController.Event> handler) {
        this.eventHandler = handler;
    }

    private void handleFieldReleased(FieldView clickedField) {
        if (selectedField != null && selectedField != clickedField) {
            if (eventHandler != null) {
                eventHandler.accept(new BoardController.MoveEvent(selectedField, clickedField));
            }
        }
    }

    private void handleFieldPressed(FieldView clickedField) {
        if (selectedField == null) {
            if (clickedField.getPieceView() != null) {
                if(eventHandler != null) {
                    eventHandler.accept(new BoardController.MarkEvent(clickedField));
                }
            }
        } else {
            if (selectedField != clickedField) {
                eventHandler.accept(new BoardController.MoveEvent(selectedField, clickedField));
            }
        }
    }

    public void setPiecesFromString(String positionString) {
        String[] positions = positionString.split(" ");
        selectedField = null;
        Platform.runLater(() -> {
            clearAllMoveHints();
            clearBoard();
            for (String position : positions) {
                char colorChar = position.charAt(0);
                char pieceChar = position.charAt(1);
                String fieldPosition = position.substring(2);

                boolean isWhite = colorChar == 'W';
                int col = FieldView.getX(fieldPosition);
                int row = FieldView.getY(fieldPosition);

                FieldView fieldView = fieldViews[col][row];
                PieceView pieceView = new PieceView(isWhite, String.valueOf(pieceChar), CELL_SIZE);
                pieceView.downSize();
                fieldView.setPieceView(pieceView);
                fieldView.getChildren().add(pieceView);
            }
        });
    }

    public void setMoveHintsFromString(String moveHints) {
        if (moveHints == null || moveHints.trim().isEmpty()) {
            return;
        }
        String[] moveHint = moveHints.split(" ");
        Platform.runLater(() -> {
            clearAllMoveHints();
            for (String position : moveHint) {
                position = position.toUpperCase();
                int col = FieldView.getX(position);
                int row = FieldView.getY(position);
                if (position.contains("0")) {
                    fieldViews[col][row].showMoveHint("GREEN");
                }
                else if (position.contains("*")) {
                    fieldViews[col][row].showMoveHint("ORANGE");
                }
                else fieldViews[col][row].showMoveHint("RED");
            }
        });
    }

    public void clearAllMoveHints() {
        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                fieldViews[i][j].clearMoveHint();
            }
        }
    }

    public void setSelectedField(FieldView selectedField) {
        this.selectedField = selectedField;
    }

    public void setPromotionBox(int col, int row) {
        FieldView fieldView = fieldViews[col][row];
        new PromotionView(this, fieldView, eventHandler, CELL_SIZE);
    }

    public void setColorBackground(String colorBackground) {
        this.setStyle("-fx-background-color: " + colorBackground + ";");
    }

    public void clearBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                FieldView fieldView = fieldViews[col][row];

                fieldView.getChildren().removeIf(child -> child instanceof PieceView);

                fieldView.setPieceView(null);
            }
        }
    }

    public void updateFieldColors(Color light, Color dark) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                FieldView field = fieldViews[col][row];
                Color color = field.isWhite() ? light : dark;
                field.updateColor(color);
            }
        }
    }

    public FieldView getFieldView(int col, int row) {
        return fieldViews[col][row];
    }

    public FieldView getSelectedField() {
        return selectedField;
    }
}