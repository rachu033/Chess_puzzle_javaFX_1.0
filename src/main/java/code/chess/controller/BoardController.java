package code.chess.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import code.chess.controller.listener.MarkPieceListener;
import code.chess.controller.listener.MovePieceListener;
import code.chess.controller.listener.PromotionChooseListener;
import code.chess.controller.listener.PromotionPawnListener;
import code.chess.model.chessgame.LogicChess;
import code.chess.model.chessgame.Field;
import code.chess.model.chessgame.LogicGame;
import code.chess.model.chessgame.LogicPuzzle;
import code.chess.view.BoardView;
import code.chess.view.FieldView;
import code.chess.view.PieceView;
import code.chess.view.SideBarView;
import javafx.application.Platform;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public abstract class BoardController implements MarkPieceListener, MovePieceListener, PromotionPawnListener, PromotionChooseListener, Runnable {
    private final BoardView boardView;
    private final LogicChess logicChess;
    private final SideBarView sideBarView;
    final BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<>(10);

    public SideBarView getSideBarView() {
        return sideBarView;
    }

    public BoardController(BoardView boardView, LogicGame gameLogic, SideBarView sideBarView) {
        this.boardView = boardView;
        this.boardView.setEventHandler(this::addEventToQueue);
        this.logicChess = gameLogic;
        this.logicChess.setEventHandler(this::addEventToQueue);
        this.boardView.setPiecesFromString(logicChess.getBoard().getCurrentPositionString());
        this.sideBarView = sideBarView;
    }

    public BoardController(BoardView boardView, LogicPuzzle chessPuzzle, SideBarView sideBarView) {
        this.boardView = boardView;
        this.boardView.setEventHandler(this::addEventToQueue);
        this.logicChess = chessPuzzle;
        this.logicChess.setEventHandler(this::addEventToQueue);
        this.boardView.setPiecesFromString(logicChess.getBoard().getCurrentPositionString());
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
        Field field = logicChess.getBoard().getField(chooseField.getX(), chooseField.getY());
        if (field.getPiece() != null && field.getPiece().isWhite() == logicChess.isWhiteTure()) {
            Platform.runLater(() -> {
                boardView.setSelectedField(chooseField);
                chooseField.getPieceView().upSize();
                Configurations configs = new Configurations();
                PropertiesConfiguration config;
                try {
                    config = configs.properties(new File("settings.properties"));
                } catch (ConfigurationException e) {
                    throw new RuntimeException(e);
                }
                boolean showMoves = config.getBoolean("showMoves", true);
                if(showMoves) {
                    boardView.setMoveHintsFromString(logicChess.getLegalMoves(chooseField.getX(), chooseField.getY()));
                }
            });
        }
    }

    @Override
    public void onMoveAttempt(FieldView currentField, FieldView targetField) {
        Platform.runLater(boardView::clearAllMoveHints);
        if(logicChess.tryMove(currentField.getX(), currentField.getY(), targetField.getX(), targetField.getY())) {
            Platform.runLater(() -> {
                boardView.setPiecesFromString(logicChess.getBoard().getCurrentPositionString());
                sideBarView.setTurn(logicChess.isWhiteTure());
                boardView.setSelectedField(null);
            });
        }
        else {
            int currentCol = currentField.getX();
            int currentRow = currentField.getY();
            int targetCol = targetField.getX();
            int targetRow = targetField.getY();
            if(logicChess.getBoard().getPieceOnField(currentCol, currentRow) != null && logicChess.getBoard().getPieceOnField(targetCol, targetRow) != null) {
                if(logicChess.getBoard().getPieceOnField(currentCol, currentRow).isWhite() == logicChess.getBoard().getPieceOnField(targetCol, targetRow).isWhite()) {
                    currentField.getPieceView().downSize();
                    boardView.setSelectedField(targetField);
                    targetField.getPieceView().upSize();
                    Configurations configs = new Configurations();
                    PropertiesConfiguration config;
                    try {
                        config = configs.properties(new File("settings.properties"));
                    } catch (ConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                    boolean showMoves = config.getBoolean("showMoves", true);
                    if(showMoves) {
                        boardView.setMoveHintsFromString(logicChess.getLegalMoves(targetCol, targetRow));
                    }
                }
                else {
                    Configurations configs = new Configurations();
                    PropertiesConfiguration config;
                    try {
                        config = configs.properties(new File("settings.properties"));
                    } catch (ConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                    boolean showMoves = config.getBoolean("showMoves", true);
                    if(showMoves) {
                        boardView.setMoveHintsFromString(logicChess.getLegalMoves(currentCol, currentRow));
                    }
                }
            }
            else {
                Configurations configs = new Configurations();
                PropertiesConfiguration config;
                try {
                    config = configs.properties(new File("settings.properties"));
                } catch (ConfigurationException e) {
                    throw new RuntimeException(e);
                }
                boolean showMoves = config.getBoolean("showMoves", true);
                if(showMoves) {
                    boardView.setMoveHintsFromString(logicChess.getLegalMoves(currentCol, currentRow));
                }
            }
        }
    }

    @Override
    public void onPromotionAttempt(Field currentField) {
        Platform.runLater(boardView::clearAllMoveHints);
        Platform.runLater(() -> boardView.setPromotionBox(currentField.getX(), currentField.getY()));
    }

    @Override
    public void onChooseAttempt(PieceView pieceView) {
        logicChess.promotePawn(pieceView.getName(), pieceView.isWhite());
        Platform.runLater(() -> boardView.setPiecesFromString(logicChess.getBoard().getCurrentPositionString()));
    }

    public LogicChess getChessLogic() {
        return logicChess;
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
