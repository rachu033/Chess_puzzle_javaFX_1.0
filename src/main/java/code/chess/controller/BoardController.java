package code.chess.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import code.chess.controller.listener.MarkPieceListener;
import code.chess.controller.listener.MovePieceListener;
import code.chess.controller.listener.PromotionChooseListener;
import code.chess.controller.listener.PromotionPawnListener;
import code.chess.model.chessgame.ChessLogic;
import code.chess.model.chessgame.Field;
import code.chess.model.chessgame.GameLogic;
import code.chess.model.chessgame.PuzzleLogic;
import code.chess.view.BoardView;
import code.chess.view.FieldView;
import code.chess.view.PieceView;
import code.chess.view.SideBarView;
import javafx.application.Platform;

public abstract class BoardController implements MarkPieceListener, MovePieceListener, PromotionPawnListener, PromotionChooseListener, Runnable {
    private final BoardView boardView;
    private final ChessLogic chessLogic;
    private final SideBarView sideBarView;
    final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(10);

    public SideBarView getSideBarView() {
        return sideBarView;
    }

    public BoardController(BoardView boardView, GameLogic gameLogic, SideBarView sideBarView) {
        this.boardView = boardView;
        this.boardView.setEventHandler(this::addEventToQueue);
        this.chessLogic = gameLogic;
        this.chessLogic.setEventHandler(this::addEventToQueue);
        this.boardView.setPiecesFromString(chessLogic.getBoard().getCurrentPositionString());
        this.sideBarView = sideBarView;
    }

    public BoardController(BoardView boardView, PuzzleLogic chessPuzzle, SideBarView sideBarView) {
        this.boardView = boardView;
        this.boardView.setEventHandler(this::addEventToQueue);
        this.chessLogic = chessPuzzle;
        this.chessLogic.setEventHandler(this::addEventToQueue);
        this.boardView.setPiecesFromString(chessLogic.getBoard().getCurrentPositionString());
        this.sideBarView = sideBarView;
    }

    public void addEventToQueue(Event event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onMarkAttempt(FieldView chooseField) {
        Field field = chessLogic.getBoard().getField(chooseField.getX(), chooseField.getY());
        if (field.getPiece() != null && field.getPiece().isWhite() == chessLogic.isWhiteTure()) {
            Platform.runLater(() -> {
                boardView.setSelectedField(chooseField);
                chooseField.getPieceView().upSize();
            });
        }
    }

    @Override
    public void onMoveAttempt(FieldView currentField, FieldView targetField) {
        if(chessLogic.tryMove(currentField.getX(), currentField.getY(), targetField.getX(), targetField.getY())) {
            Platform.runLater(() -> {
                boardView.setPiecesFromString(chessLogic.getBoard().getCurrentPositionString());
                sideBarView.setTurn(chessLogic.isWhiteTure());
                boardView.setSelectedField(null);
            });
        }
        else {
            int currentCol = currentField.getX();
            int currentRow = currentField.getY();
            int targetCol = targetField.getX();
            int targetRow = targetField.getY();
            if(chessLogic.getBoard().getPieceOnField(currentCol, currentRow) != null && chessLogic.getBoard().getPieceOnField(targetCol, targetRow) != null) {
                if(chessLogic.getBoard().getPieceOnField(currentCol, currentRow).isWhite() == chessLogic.getBoard().getPieceOnField(targetCol, targetRow).isWhite()) {
                    currentField.getPieceView().downSize();
                    boardView.setSelectedField(targetField);
                    targetField.getPieceView().upSize();
                }
            }
        }
    }

    @Override
    public void onPromotionAttempt(Field currentField) {
        Platform.runLater(() -> boardView.setPromotionBox(currentField.getX(), currentField.getY()));
    }

    @Override
    public void onChooseAttempt(PieceView pieceView) {
        chessLogic.promotePawn(pieceView.getName(), pieceView.isWhite());
        Platform.runLater(() -> boardView.setPiecesFromString(chessLogic.getBoard().getCurrentPositionString()));
    }

    public ChessLogic getChessLogic() {
        return chessLogic;
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public static class MarkEvent extends Event {
        private final FieldView chooseField;

        public MarkEvent(FieldView chooseField) {
            this.chooseField = chooseField;
        }

        public FieldView getChooseField() {
            return chooseField;
        }
    }

    public static class MoveEvent extends Event {
        private final FieldView currentField;
        private final FieldView targetField;

        public MoveEvent(FieldView currentField, FieldView targetField) {
            this.currentField = currentField;
            this.targetField = targetField;
        }

        public FieldView getCurrentField() {
            return currentField;
        }

        public FieldView getTargetField() {
            return targetField;
        }
    }

    public static class PromotionEvent extends Event {
        private final Field chooseField;

        public PromotionEvent(Field chooseField) {
            this.chooseField = chooseField;
        }

        public Field getChooseField() {
            return chooseField;
        }
    }

    public static class ChooseEvent extends Event {
        private final PieceView pieceView;

        public ChooseEvent(PieceView pieceView) {
            this.pieceView = pieceView;
        }

        public PieceView getPieceView() {
            return pieceView;
        }
    }

    public static abstract class Event { }
}
