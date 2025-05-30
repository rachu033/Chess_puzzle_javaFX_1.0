package code.chess.view;

import code.chess.controller.ApplicationController;
import code.chess.controller.GameController;
import code.chess.model.puzzle.Puzzle;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public class SideBarView extends VBox {
    private final Button newGameButton = new Button("Gracz vs Gracz");
    private final Button newPuzzleButton = new Button("Wylosuj zagadkę");
    private final Button copyPgnButton = new Button("Kopiuj PGN");
    private final Button settingsButton = new Button("⚙ Ustawienia");
    private final Button aboutButton = new Button("Autor");
    private final PuzzleListView puzzleListView = new PuzzleListView();
    private final InfoBox infoBox = new InfoBox();
    private Consumer<ApplicationController.Event> eventHandlerApplication;
    private Consumer<GameController.Event> eventHandlerGame;

    private static final String BUTTON_STYLE = """
        -fx-background-color: #3a3a3a;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 0;
        -fx-border-color: transparent;
        -fx-border-width: 2;
        -fx-focus-color: #2e8b57;
        -fx-faint-focus-color: rgba(46,139,87,0.4);
    """;

    private static final String BUTTON_HOVER_STYLE = BUTTON_STYLE + "-fx-opacity: 0.8;";

    private void styleButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(50.0);
        button.setStyle(BUTTON_STYLE);
        button.setOnMouseEntered(_ -> button.setStyle(BUTTON_HOVER_STYLE));
        button.setOnMouseExited(_ -> button.setStyle(BUTTON_STYLE));
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

        styleButton(newGameButton);
        styleButton(newPuzzleButton);
        styleButton(copyPgnButton);
        styleButton(settingsButton);
        styleButton(aboutButton);

        VBox.setMargin(newGameButton, new javafx.geometry.Insets(0, 5, 0, 5));
        newGameButton.setOnAction(_ -> {
            if (eventHandlerApplication != null) {
                eventHandlerApplication.accept(new ApplicationController.NewGameEvent());
            }
        });

        VBox.setMargin(newPuzzleButton, new javafx.geometry.Insets(0, 5, 0, 5));
        newPuzzleButton.setOnAction(_ -> {
            if (eventHandlerApplication != null) {
                eventHandlerApplication.accept(new ApplicationController.NewPuzzleEvent());
            }
        });

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

        VBox.setMargin(settingsButton, new javafx.geometry.Insets(0, 5, 0, 5));
        settingsButton.setOnAction(_ -> eventHandlerApplication.accept(new ApplicationController.OpenSettingsEvent()));

        VBox.setMargin(aboutButton, new javafx.geometry.Insets(0, 5, 0, 5));

        aboutButton.setOnAction(_ -> eventHandlerApplication.accept(new ApplicationController.OpenAuthorEvent()));

        this.getChildren().addAll(newGameButton, newPuzzleButton, puzzleListView, infoBox, copyPgnButton, settingsButton, aboutButton);
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

    public PuzzleListView getPuzzleListView() {
        return puzzleListView;
    }

    public void setNewPuzzleButtonLoad() {
        newPuzzleButton.setText("Ładowanie...");
        newPuzzleButton.setDisable(true);
    }

    public void setNewPuzzleButtonException() {
        newPuzzleButton.setStyle(
                BUTTON_STYLE.replace("-fx-background-color: #3a3a3a;", "-fx-background-color: RED;")
        );
        newPuzzleButton.setText("Błąd pobierania zagadki");
        newPuzzleButton.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(_ -> setNewPuzzleButton());
        pause.play();
    }

    public void setNewPuzzleButton() {
        newPuzzleButton.setStyle(BUTTON_STYLE);
        newPuzzleButton.setText("Nowa zagadka");
        newPuzzleButton.setDisable(false);
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

    public void setSolved() {
        infoBox.setInfo("Zagadka rozwiązana", "green", "white");
    }
}