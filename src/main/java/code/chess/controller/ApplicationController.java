package code.chess.controller;

import code.chess.controller.listener.LoadPuzzleListener;
import code.chess.controller.listener.NewGameListener;
import code.chess.controller.listener.NewPuzzleListener;
import code.chess.model.ApplicationModel;
import code.chess.model.chessgame.GameLogic;
import code.chess.model.chessgame.PuzzleLogic;
import code.chess.model.database.PuzzleDAO;
import code.chess.model.puzzle.Puzzle;
import code.chess.model.puzzle.PuzzleFetcher;
import code.chess.view.ApplicationView;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.ObjectUtils;
import java.util.List;
import java.util.concurrent.*;

public class ApplicationController implements NewGameListener, NewPuzzleListener, LoadPuzzleListener, Runnable {
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

    @Override
    public void newGame() {
        cancelCurrentTask();

        Platform.runLater(() -> applicationView.getSideBarView().getPuzzleListView().setSelectedView(null));

        currentTask = executorService.submit(() -> {
                if (Thread.currentThread().isInterrupted()) return;
                applicationModel.setChessLogic(new GameLogic());
                GameController gameController = new GameController(
                        applicationView.getBoardView(),
                        (GameLogic) applicationModel.getChessLogic(),
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

            Puzzle puzzle;
            try {
                puzzle = PuzzleFetcher.fetchPuzzle();
                PuzzleDAO puzzleDAO = new PuzzleDAO();
                puzzleDAO.savePuzzle(puzzle);
                Platform.runLater(() -> applicationView.getSideBarView().addPuzzleToList(puzzle));
            } catch (InterruptedException e) {
                Platform.runLater(() -> applicationView.getSideBarView().setNewPuzzleButton());
                return;
            } catch (Exception e) {
                Platform.runLater(() -> applicationView.getSideBarView().setNewPuzzleButtonException());
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                Platform.runLater(() -> applicationView.getSideBarView().setNewPuzzleButton());
                return;
            }

            applicationModel.setChessLogic(new PuzzleLogic(puzzle));
            PuzzleController puzzleController = new PuzzleController(
                    applicationView.getBoardView(),
                    (PuzzleLogic) applicationModel.getChessLogic(),
                    applicationView.getSideBarView()
            );
            puzzleController.setApplicationEventQueue(this::addEventToQueue);

            currentTask = executorService.submit(puzzleController);

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

            applicationModel.setChessLogic(new PuzzleLogic(puzzle));
            PuzzleController puzzleController = new PuzzleController(
                    applicationView.getBoardView(),
                    (PuzzleLogic) applicationModel.getChessLogic(),
                    applicationView.getSideBarView()
            );
            puzzleController.setApplicationEventQueue(this::addEventToQueue);

            currentTask = executorService.submit(puzzleController);
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

    public static abstract class Event { }
}
