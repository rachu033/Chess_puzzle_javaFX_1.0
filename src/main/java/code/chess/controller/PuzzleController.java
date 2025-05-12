package code.chess.controller;

import code.chess.controller.listener.SuccessSolveListener;
import code.chess.controller.listener.WrongSolveListener;
import code.chess.model.chessgame.Field;
import code.chess.model.chessgame.PuzzleLogic;
import code.chess.model.database.PuzzleDAO;
import code.chess.model.puzzle.Puzzle;
import code.chess.view.BoardView;
import code.chess.view.FieldView;
import code.chess.view.SideBarView;
import javafx.application.Platform;
import java.util.function.Consumer;

public class PuzzleController extends BoardController implements WrongSolveListener, SuccessSolveListener {
    private volatile boolean running = true;
    private Consumer<ApplicationController.Event> applicationEventQueue;
    private final boolean isWhitePuzzle;

    public PuzzleController(BoardView boardView, PuzzleLogic puzzleLogic, SideBarView sideBarView) {
        super(boardView, puzzleLogic, sideBarView);
        puzzleLogic.loadFromPgnMoves(puzzleLogic.getPuzzle().getPgn());
        boardView.setPiecesFromString(puzzleLogic.getBoard().getCurrentPositionString());
        isWhitePuzzle = puzzleLogic.getPuzzle().getPgn().trim().split("\\s+").length % 2 == 0;
        Platform.runLater(() -> sideBarView.setTurn(getChessLogic().isWhiteTure()));
    }

    public void setApplicationEventQueue(Consumer<ApplicationController.Event> handler) {
        this.applicationEventQueue = handler;
    }

    private void handleEvent(Event event) {


        if (event instanceof MarkEvent markEvent) {
            onMarkAttempt(markEvent.getChooseField());
        } else if (event instanceof MoveEvent moveEvent) {
            onMoveAttempt(moveEvent.getCurrentField(), moveEvent.getTargetField());
        } else if (event instanceof MoveSimulationEvent moveSimulationEvent) {
            FieldView currentFieldView = getBoardView().getFieldView(moveSimulationEvent.getCurrentField().getX(), moveSimulationEvent.getCurrentField().getY());
            FieldView targetFieldView = getBoardView().getFieldView(moveSimulationEvent.getTargetField().getX(), moveSimulationEvent.getTargetField().getY());
            onMoveAttempt(currentFieldView, targetFieldView);
        } else if (event instanceof PromotionEvent promotionEvent) {
            onPromotionAttempt(promotionEvent.getChooseField());
        } else if (event instanceof ChooseEvent chooseEvent) {
            onChooseAttempt(chooseEvent.getPieceView());
        } else if (event instanceof WrongEvent) {
            onWrongAttempt();
        } else if (event instanceof SuccessEvent successEvent) {
            onSuccessAttempt(successEvent.getPuzzle());
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    Event event = eventQueue.take();
                    handleEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> getBoardView().setColorBackground("#3e2723"));
            }).start();
        }
    }

    @Override
    public void onMarkAttempt(FieldView chooseField) {
        Field field = getChessLogic().getBoard().getField(chooseField.getX(), chooseField.getY());
        if (field.getPiece() != null && field.getPiece().isWhite() == getChessLogic().isWhiteTure() && field.getPiece().isWhite() == isWhitePuzzle) {
            Platform.runLater(() -> {
                getBoardView().setSelectedField(chooseField);
                chooseField.getPieceView().upSize();
            });
        }
    }

    @Override
    public void onWrongAttempt() {
        Platform.runLater(() -> getBoardView().setColorBackground("red"));
        new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> getBoardView().setColorBackground("#3e2723"));
        }).start();
    }

    @Override
    public void onSuccessAttempt(Puzzle puzzle) {
        puzzle.setSolved(true);
        PuzzleDAO puzzleDAO = new PuzzleDAO();
        puzzleDAO.markPuzzleAsSolved(puzzle.getPgn());
        Platform.runLater(() -> getBoardView().setColorBackground("green"));
        Platform.runLater(() -> getSideBarView().updatePuzzleSolved(puzzle));
        applicationEventQueue.accept(new ApplicationController.NewPuzzleEvent());
        running = false;
    }

    public static class MoveSimulationEvent extends Event {
        private final Field currentField;
        private final Field targetField;

        public MoveSimulationEvent(Field currentField, Field targetField) {
            this.currentField = currentField;
            this.targetField = targetField;
        }

        public Field getCurrentField() {
            return currentField;
        }

        public Field getTargetField() {
            return targetField;
        }
    }

    public static class WrongEvent extends Event {
        public WrongEvent() {}
    }

    public static class SuccessEvent extends Event {
        private final Puzzle puzzle;

        public SuccessEvent(Puzzle puzzle) {
            this.puzzle = puzzle;
        }

        public Puzzle getPuzzle() {
            return puzzle;
        }
    }
}