package code.chess.view;

import code.chess.controller.ApplicationController;
import code.chess.model.puzzle.Puzzle;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PuzzleListView extends VBox {
    private final ListView<PuzzleView> puzzleList = new ListView<>();
    private final List<PuzzleView> masterList = new ArrayList<>();

    private final Button sortByRatingButton = new Button("Ranking ↑");
    private final Button sortByDateButton = new Button("Data ↑");
    private final Button sortBySolvedButton = new Button("Wszystkie");

    private boolean sortByRatingAsc = true;
    private boolean sortByDateAsc = false;

    private enum SortMode { RATING, DATE }
    private enum FilterMode { ALL, SOLVED, UNSOLVED, FAVOURITES }

    private SortMode activeSortMode = SortMode.DATE;
    private FilterMode filterMode = FilterMode.ALL;

    private Consumer<ApplicationController.Event> eventHandler;
    private PuzzleView selectedView;

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

    public PuzzleListView() {
        initializeView();
        applyCurrentSorting();
        updateSortButtonStyles();
        updateSolvedFilterButtonStyle();
    }

    public void setEventHandler(Consumer<ApplicationController.Event> handler) {
        this.eventHandler = handler;
    }

    private void styleButton(Button button) {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(50.0);
        button.setStyle(BUTTON_STYLE);
        button.setOnMouseEntered(_ -> {
            String style = button.getStyle();
            button.setStyle(style + "-fx-opacity: 0.8;");
        });
        button.setOnMouseExited(_ -> {
            updateSolvedFilterButtonStyle();
            updateSortButtonStyles();
        });
    }

    private void initializeView() {
        puzzleList.setPrefHeight(420);
        puzzleList.setStyle("""
            -fx-selection-bar: transparent;
            -fx-selection-bar-non-focused: transparent;
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
        """);

        Platform.runLater(() -> {
            javafx.scene.Node scrollBar = puzzleList.lookup(".scroll-bar");
            if (scrollBar != null) {
                scrollBar.setStyle("-fx-background-color: transparent; -fx-background-radius: 0px;");
            }

            javafx.scene.Node thumb = puzzleList.lookup(".scroll-bar .thumb");
            if (thumb != null) {
                thumb.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 0px;");
            }

            javafx.scene.Node track = puzzleList.lookup(".scroll-bar .track");
            if (track != null) {
                track.setStyle("-fx-background-color: transparent; -fx-border-color: #d3d3d3; -fx-border-width: 1px;");
            }

            javafx.scene.Node incrementButton = puzzleList.lookup(".scroll-bar .increment-button");
            if (incrementButton != null) {
                incrementButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            }

            javafx.scene.Node decrementButton = puzzleList.lookup(".scroll-bar .decrement-button");
            if (decrementButton != null) {
                decrementButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            }
        });

        styleButton(sortByRatingButton);
        styleButton(sortByDateButton);
        styleButton(sortBySolvedButton);

        sortByRatingButton.setPrefWidth(95);
        sortByDateButton.setPrefWidth(95);

        sortByRatingButton.setOnAction(_ -> {
            sortByRatingAsc = !sortByRatingAsc;
            activeSortMode = SortMode.RATING;
            sortByRatingButton.setText(sortByRatingAsc ? "Ranking ↑" : "Ranking ↓");
            applyCurrentSorting();
            updateSortButtonStyles();
        });

        sortByDateButton.setOnAction(_ -> {
            sortByDateAsc = !sortByDateAsc;
            activeSortMode = SortMode.DATE;
            sortByDateButton.setText(sortByDateAsc ? "Data ↑" : "Data ↓");
            applyCurrentSorting();
            updateSortButtonStyles();
        });

        sortBySolvedButton.setOnAction(_ -> {
            switch (filterMode) {
                case ALL -> {
                    filterMode = FilterMode.UNSOLVED;
                    sortBySolvedButton.setText("Nierozwiązane");
                }
                case UNSOLVED -> {
                    filterMode = FilterMode.SOLVED;
                    sortBySolvedButton.setText("Rozwiązane");
                }
                case SOLVED -> {
                    filterMode = FilterMode.FAVOURITES;
                    sortBySolvedButton.setText("Ulubione");
                }
                case FAVOURITES -> {
                    filterMode = FilterMode.ALL;
                    sortBySolvedButton.setText("Wszystkie");
                }
            }
            applyCurrentSorting();
            updateSolvedFilterButtonStyle();
        });

        HBox sortingBox = new HBox(10, sortByRatingButton, sortByDateButton);
        HBox.setHgrow(sortByRatingButton, Priority.ALWAYS);
        HBox.setHgrow(sortByDateButton, Priority.ALWAYS);
        VBox.setMargin(sortingBox, new javafx.geometry.Insets(0, 5, 0, 5));
        VBox.setMargin(sortBySolvedButton, new javafx.geometry.Insets(0, 5, 0, 5));

        this.setSpacing(10);
        this.getChildren().addAll(sortingBox, sortBySolvedButton, puzzleList);
    }

    public void addPuzzleOnLaunch(Puzzle puzzle) {
        PuzzleView puzzleView = createPuzzleView(puzzle);
        puzzleView.setEventHandler(eventHandler);
        masterList.add(puzzleView);
        applyCurrentSorting();
    }

    public void addPuzzle(Puzzle puzzle) {
        PuzzleView puzzleView = createPuzzleView(puzzle);
        puzzleView.setEventHandler(eventHandler);
        masterList.add(puzzleView);
        applyCurrentSorting();
        selectPuzzleView(puzzleView);
    }

    private PuzzleView createPuzzleView(Puzzle puzzle) {
        PuzzleView puzzleView = new PuzzleView(puzzle);
        puzzleView.setOnMouseClicked(_ -> {
            selectPuzzleView(puzzleView);
            if (eventHandler != null) {
                eventHandler.accept(new ApplicationController.LoadPuzzleEvent(puzzle.getPgn()));
            }
        });
        return puzzleView;
    }

    private void selectPuzzleView(PuzzleView puzzleView) {
        if (selectedView != null) {
            selectedView.setSelected(false);
        }
        selectedView = puzzleView;
        puzzleView.setSelected(true);
    }

    public void addPuzzles(List<Puzzle> puzzles) {
        for (Puzzle puzzle : puzzles) {
            addPuzzleOnLaunch(puzzle);
        }
    }

    public void updatePuzzleSolved(Puzzle puzzle) {
        for (PuzzleView view : masterList) {
            if (Objects.equals(view.getPuzzle().getPgn(), puzzle.getPgn())) {
                view.getPuzzle().setSolved(true);
                view.updateSolvedColor();
                break;
            }
        }
        applyCurrentSorting();
    }

    private void applyCurrentSorting() {
        List<PuzzleView> filtered = new ArrayList<>(masterList);

        filtered.removeIf(pv -> switch (filterMode) {
            case ALL -> false;
            case SOLVED -> !pv.getPuzzle().isSolved();
            case UNSOLVED -> pv.getPuzzle().isSolved();
            case FAVOURITES -> !pv.getPuzzle().isFavourite();
        });

        filtered.sort((a, b) -> {
            int cmp = switch (activeSortMode) {
                case RATING -> Integer.compare(a.getPuzzle().getRating(), b.getPuzzle().getRating());
                case DATE -> a.getPuzzle().getDateTime().compareTo(b.getPuzzle().getDateTime());
            };
            boolean asc = activeSortMode == SortMode.RATING ? sortByRatingAsc : sortByDateAsc;
            return asc ? cmp : -cmp;
        });

        puzzleList.getItems().setAll(filtered);
    }

    private void updateSortButtonStyles() {
        String activeStyle = BUTTON_STYLE.replace("-fx-background-color: #3a3a3a;", "-fx-background-color: GREEN;");

        sortByRatingButton.setStyle(activeSortMode == SortMode.RATING ? activeStyle : BUTTON_STYLE);
        sortByDateButton.setStyle(activeSortMode == SortMode.DATE ? activeStyle : BUTTON_STYLE);
    }

    private void updateSolvedFilterButtonStyle() {
        String solvedStyle = BUTTON_STYLE
                .replace("-fx-background-color: #3a3a3a;", "-fx-background-color: GREEN;")
                .replace("-fx-border-color: transparent;", "-fx-border-color: #3a3a3a;");

        String unsolvedStyle = BUTTON_STYLE
                .replace("-fx-background-color: #3a3a3a;", "-fx-background-color: ORANGE;")
                .replace("-fx-border-color: transparent;", "-fx-border-color: #3a3a3a;");

        String favouriteStyle = BUTTON_STYLE
                .replace("-fx-background-color: #3a3a3a;", "-fx-background-color: #D4AF37;") // Gold
                .replace("-fx-border-color: transparent;", "-fx-border-color: #3a3a3a;");

        sortBySolvedButton.setStyle(
                switch (filterMode) {
                    case ALL -> BUTTON_STYLE;
                    case UNSOLVED -> unsolvedStyle;
                    case SOLVED -> solvedStyle;
                    case FAVOURITES -> favouriteStyle;
                }
        );
    }

    public void setSelectedView(PuzzleView selectedView) {
        if(this.selectedView != null) {
            this.selectedView.setSelected(false);
        }
        this.selectedView = selectedView;
        if(selectedView != null){
            selectedView.setSelected(true);
        }
    }

    public PuzzleView getSelectedView() {
        return selectedView;
    }
}
