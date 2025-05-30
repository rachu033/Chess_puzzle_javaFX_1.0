package code.chess.controller;

import code.chess.controller.listener.*;
import code.chess.model.ApplicationModel;
import code.chess.model.chessgame.LogicGame;
import code.chess.model.chessgame.LogicPuzzle;
import code.chess.model.database.PuzzleDAO;
import code.chess.model.puzzle.Puzzle;
import code.chess.model.puzzle.PuzzleFetcher;
import code.chess.view.ApplicationView;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

public class ApplicationController implements NewGameListener, NewPuzzleListener, LoadPuzzleListener, SetFavouriteListener, OpenSettingsListener, OpenAuthorListener, Runnable {
    private static final Log logger = LogFactory.getLog(ApplicationController.class);
    private final ApplicationView applicationView;
    private final ApplicationModel applicationModel;

    private final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(10);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> currentTask;

    public ApplicationController(Stage primaryStage) {
        applicationModel = new ApplicationModel();
        applicationView = new ApplicationView(primaryStage);
        applicationView.getSideBarView().setEventHandlerApplication(this::addEventToQueue);

        PuzzleDAO puzzleDAO = new PuzzleDAO();
        List<Puzzle> puzzles = puzzleDAO.getAllPuzzles();
        applicationView.getSideBarView().addPuzzlesToList(puzzles);
        loadInitialSettings();
    }

    public void addEventToQueue(Event event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void handleEvent(Event event) {
        if (event instanceof NewPuzzleEvent) {
            newPuzzle();
        } else if (event instanceof LoadPuzzleEvent loadPuzzleEvent) {
            loadPuzzle(loadPuzzleEvent.getPgn());
        } else if (event instanceof NewGameEvent) {
            newGame();
        } else if (event instanceof SetFavouriteEvent setFavouriteEvent) {
            onSetFavourite(setFavouriteEvent.getPuzzle());
        } else if (event instanceof OpenSettingsEvent) {
            onOpenSettings();
        } else if (event instanceof OpenAuthorEvent) {
            onOpenAuthor();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = eventQueue.take();
                handleEvent(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void cancelCurrentTask() {
        if (!ObjectUtils.isEmpty(currentTask) && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    private void loadInitialSettings() {
        Configurations configs = new Configurations();
        File configFile = new File("settings.properties");

        try {
            PropertiesConfiguration config = configs.properties(configFile);

            String lightHex = config.getString("lightSquare", "#f5f5dc");
            String darkHex = config.getString("darkSquare", "#a52a2a");

            Color light = Color.web(lightHex);
            Color dark = Color.web(darkHex);

            applicationView.getBoardView().updateFieldColors(light, dark);

        } catch (ConfigurationException e) {
            e.printStackTrace();

            applicationView.getBoardView().updateFieldColors(Color.BEIGE, Color.BROWN);
        }
    }

    @Override
    public void newGame() {
        cancelCurrentTask();

        Platform.runLater(() -> applicationView.getSideBarView().getPuzzleListView().setSelectedView(null));

        currentTask = executorService.submit(() -> {
                if (Thread.currentThread().isInterrupted()) return;
                applicationModel.setChessLogic(new LogicGame());
                GameController gameController = new GameController(
                        applicationView.getBoardView(),
                        (LogicGame) applicationModel.getChessLogic(),
                        applicationView.getSideBarView()
                );

                currentTask = executorService.submit(gameController);
        });
    }

    @Override
    public void newPuzzle() {
        cancelCurrentTask();

        Platform.runLater(() -> applicationView.getSideBarView().setNewPuzzleButtonLoad());

        currentTask = executorService.submit(() -> {
            try {
                if (Thread.interrupted()) throw new InterruptedException();

                Puzzle puzzle = PuzzleFetcher.fetchPuzzle();

                if (Thread.interrupted()) throw new InterruptedException();

                PuzzleDAO puzzleDAO = new PuzzleDAO();
                puzzleDAO.savePuzzle(puzzle);

                Platform.runLater(() -> applicationView.getSideBarView().addPuzzleToList(puzzle));

                if (Thread.interrupted()) throw new InterruptedException();

                applicationModel.setChessLogic(new LogicPuzzle(puzzle));
                PuzzleController puzzleController = new PuzzleController(
                        applicationView.getBoardView(),
                        (LogicPuzzle) applicationModel.getChessLogic(),
                        applicationView.getSideBarView()
                );
                puzzleController.setApplicationEventQueue(this::addEventToQueue);

                currentTask = executorService.submit(puzzleController);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // reassert the interrupted flag
                logger.info("Puzzle loading was interrupted.");
            } catch (Exception e) {
                logger.error("Error during puzzle loading", e);
                Platform.runLater(() -> applicationView.getSideBarView().setNewPuzzleButtonException());
                return;
            }

            Platform.runLater(() -> applicationView.getSideBarView().setNewPuzzleButton());
        });
    }

    @Override
    public void loadPuzzle(String pgn) {
        cancelCurrentTask();

        currentTask = executorService.submit(() -> {
            Puzzle puzzle;
            try {
                PuzzleDAO puzzleDAO = new PuzzleDAO();
                puzzle = puzzleDAO.getPuzzleByPgn(pgn);
            } catch (Exception e) {
                logger.error("Error getting puzzle: " + pgn + " from database", e);
                return;
            }

            if (Thread.currentThread().isInterrupted()) return;

            applicationModel.setChessLogic(new LogicPuzzle(puzzle));
            PuzzleController puzzleController = new PuzzleController(
                    applicationView.getBoardView(),
                    (LogicPuzzle) applicationModel.getChessLogic(),
                    applicationView.getSideBarView()
            );
            puzzleController.setApplicationEventQueue(this::addEventToQueue);

            currentTask = executorService.submit(puzzleController);
        });
    }

    @Override
    public void onSetFavourite(Puzzle puzzle) {
        if(puzzle.isFavourite()) {
            puzzle.setFavourite(true);
            PuzzleDAO puzzleDAO = new PuzzleDAO();
            puzzleDAO.addToFavorites(puzzle.getPgn());
        }
        else {
            puzzle.setFavourite(false);
            PuzzleDAO puzzleDAO = new PuzzleDAO();
            puzzleDAO.removeFromFavorites(puzzle.getPgn());
        }
    }

    @Override
    public void onOpenSettings() {
        Platform.runLater(() -> {
            if(applicationView.getBoardView().getSelectedField() != null && applicationView.getBoardView().getSelectedField().getPieceView() != null) {
                applicationView.getBoardView().getSelectedField().getPieceView().downSize();
            }
            applicationView.getBoardView().clearAllMoveHints();
            applicationView.openWindow("/code/chess/views/settings.fxml", "Ustawienia");
        });
    }

    @Override
    public void onOpenAuthor() {
        Platform.runLater(() -> {
            if(applicationView.getBoardView().getSelectedField() != null && applicationView.getBoardView().getSelectedField().getPieceView() != null) {
                applicationView.getBoardView().getSelectedField().getPieceView().downSize();
            }
            applicationView.getBoardView().clearAllMoveHints();
            applicationView.openWindow("/code/chess/views/author.fxml", "Adam Rachuba, WCY22IJ3S1");
        });
    }

    public static class NewGameEvent extends Event { }

    public static class NewPuzzleEvent extends Event { }

    public static class LoadPuzzleEvent extends Event {
        private final String pgn;

        public LoadPuzzleEvent(String pgn) {
            this.pgn = pgn;
        }

        public String getPgn() {
            return pgn;
        }
    }

    public static class SetFavouriteEvent extends Event {
        private final Puzzle puzzle;

        public SetFavouriteEvent(Puzzle puzzle) {
            this.puzzle = puzzle;
        }

        public Puzzle getPuzzle() {
            return puzzle;
        }
    }

    public static class OpenSettingsEvent extends Event { }

    public static class OpenAuthorEvent extends Event { }

    public static abstract class Event { }
}
