package code.chess.view;

import code.chess.controller.ApplicationController;
import code.chess.controller.GameController;
import code.chess.model.puzzle.Puzzle;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.List;
import java.util.function.Consumer;

public class SideBarView extends VBox {
    private final Button newGameButton = new Button("Gracz vs Gracz");
    private final Button newPuzzleButton = new Button("Wylosuj zagadkę");
    private final Button copyPgnButton = new Button("Kopiuj PGN");
    private final PuzzleListView puzzleListView = new PuzzleListView();
    private final InfoBox infoBox = new InfoBox();
    private Consumer<ApplicationController.Event> eventHandlerApplication;
    private Consumer<GameController.Event> eventHandlerGame;

    public PuzzleListView getPuzzleListView() {
        return puzzleListView;
    }

    public SideBarView(double width) {
        this.setPrefWidth(width);
        initializeSidebar();
    }

    public void setEventHandlerApplication(Consumer<ApplicationController.Event> handler) {
        this.eventHandlerApplication = handler;
        puzzleListView.setEventHandler(handler);
    }

    public void setEventHandlerGame(Consumer<GameController.Event> handler) {
        this.eventHandlerGame = handler;
    }

    private void initializeSidebar() {
        this.setSpacing(10);

        newGameButton.setMaxWidth(Double.MAX_VALUE);
        newGameButton.setPrefHeight(50);
        setNewGameButton();
        VBox.setMargin(newGameButton, new javafx.geometry.Insets(0, 5, 0, 5));
        newGameButton.setOnAction(_ -> {
            if (eventHandlerApplication != null) {
                eventHandlerApplication.accept(new ApplicationController.NewGameEvent());
            }
        });

        newPuzzleButton.setMaxWidth(Double.MAX_VALUE);
        newPuzzleButton.setPrefHeight(50);
        setNewPuzzleButton();
        VBox.setMargin(newPuzzleButton, new javafx.geometry.Insets(0, 5, 0, 5));
        newPuzzleButton.setOnAction(_ -> {
            if (eventHandlerApplication != null) {
                eventHandlerApplication.accept(new ApplicationController.NewPuzzleEvent());
            }
        });

        copyPgnButton.setMaxWidth(Double.MAX_VALUE);
        copyPgnButton.setPrefHeight(50);
        copyPgnButton.setStyle("-fx-background-color: gray; -fx-alignment: center; -fx-background-radius: 0; -fx-text-fill: white; -fx-font-weight: bold;");
        copyPgnButton.setOnAction(_ -> {
            if (puzzleListView.getSelectedView() != null) {
                String pgn = puzzleListView.getSelectedView().getPuzzle().getPgn();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(pgn);
                clipboard.setContent(content);
            }
            else {
                if(eventHandlerGame != null) eventHandlerGame.accept(new GameController.CopyPgnEvent());
            }
        });
        VBox.setMargin(copyPgnButton, new javafx.geometry.Insets(0, 5, 0, 5));

        VBox.setMargin(infoBox, new javafx.geometry.Insets(0, 5, 0, 5));
        infoBox.setDefaultInfo("Czyj ruch?");

        this.getChildren().addAll(newGameButton, newPuzzleButton, puzzleListView, infoBox, copyPgnButton);
    }

    public void addPuzzleToList(Puzzle puzzle) {
        puzzleListView.addPuzzle(puzzle);
    }

    public void addPuzzlesToList(List<Puzzle> puzzles) {
        puzzleListView.addPuzzles(puzzles);
    }

    public void updatePuzzleSolved(Puzzle puzzle) {
        puzzleListView.updatePuzzleSolved(puzzle);
    }

    public void setNewPuzzleButtonLoad() {
        newPuzzleButton.setStyle(
                "-fx-background-radius: 0;" +
                        "-fx-background-color: GREEN;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 2;" +
                        "-fx-focus-color: #2e8b57;" +
                        "-fx-faint-focus-color: rgba(46,139,87,0.4);"
        );
        newPuzzleButton.setText("Ładowanie...");
        newPuzzleButton.setDisable(true);
    }

    public void setNewPuzzleButtonException() {
        newPuzzleButton.setStyle(
                "-fx-background-radius: 0;" +
                        "-fx-background-color: RED;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 2;" +
                        "-fx-focus-color: #2e8b57;" +
                        "-fx-faint-focus-color: rgba(46,139,87,0.4);"
        );
        newPuzzleButton.setText("Błąd pobierania zagadki");
        newPuzzleButton.setDisable(false);
    }

    public void setNewPuzzleButton() {
        newPuzzleButton.setStyle(
                "-fx-background-radius: 0;" +
                        "-fx-background-color: #3a3a3a;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 2;" +
                        "-fx-focus-color: #2e8b57;" +
                        "-fx-faint-focus-color: rgba(46,139,87,0.4);"
        );
        newPuzzleButton.setText("Nowa zagadka");
        newPuzzleButton.setDisable(false);
    }

    public void setNewGameButton() {
        newGameButton.setStyle(
                "-fx-background-radius: 0;" +
                        "-fx-background-color: #3a3a3a;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 2;" +
                        "-fx-focus-color: #2e8b57;" +
                        "-fx-faint-focus-color: rgba(46,139,87,0.4);"
        );
        newGameButton.setText("Gracz vs Gracz");
        newGameButton.setDisable(false);
    }

    public void setTurn(boolean isWhite) {
        if (isWhite) {
            infoBox.setInfo("Ruch białych", "#DCDCDC", "#333333");
        } else {
            infoBox.setInfo("Ruch czarnych", "#2f2f2f", "white");
        }
    }

    public void setWin(boolean isWhite) {
        if (isWhite) {
            infoBox.setInfo("Szach mat!", "#DCDCDC", "#333333");
        } else {
            infoBox.setInfo("Szach mat!", "#2f2f2f", "white");
        }
    }

    public void setDraw(int typeDraw) {
        if(typeDraw == 0) {
            infoBox.setInfo("Szach! mat? I Pat :D", "#DCDCDC", "gray");
        }
    }
}