package code.chess.controller;

import code.chess.controller.listener.CheckMateListener;
import code.chess.controller.listener.CopyPgnListener;
import code.chess.controller.listener.DrawListener;
import code.chess.model.chessgame.GameLogic;
import code.chess.view.BoardView;
import code.chess.view.SideBarView;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class GameController extends BoardController implements CheckMateListener, DrawListener, CopyPgnListener {
    boolean isCheckMate;

    public GameController(BoardView boardView, GameLogic gameLogic, SideBarView sideBarView) {
        super(boardView, gameLogic, sideBarView);
        sideBarView.setEventHandlerGame(this::handleEvent);
        this.isCheckMate = false;
    }

    public void handleEvent(Event event) {
        if (event instanceof MarkEvent markEvent) {
            onMarkAttempt(markEvent.getChooseField());
        } else if (event instanceof MoveEvent moveEvent) {
            onMoveAttempt(moveEvent.getCurrentField(), moveEvent.getTargetField());
        } else if (event instanceof PromotionEvent promotionEvent) {
            onPromotionAttempt(promotionEvent.getChooseField());
        } else if (event instanceof ChooseEvent chooseEvent) {
            onChooseAttempt(chooseEvent.getPieceView());
        } else if (event instanceof CheckMateEvent checkMateEvent) {
            onCheckMateAttempt(checkMateEvent.isWhite());
        } else if (event instanceof DrawEvent drawEvent) {
            onDrawAttempt(drawEvent.getTypeDraw());
        } else if (event instanceof CopyPgnEvent) {
            onCopyPgnAttempt();
        }
    }

    @Override
    public void run() {
        try {
            while (!isCheckMate) {
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
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> getBoardView().setColorBackground("#3e2723"));
            }).start();
        }
        try {
            while (!Thread.interrupted()){
                eventQueue.take();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onCheckMateAttempt(boolean isWhite) {
        Platform.runLater(() -> {
            getBoardView().setColorBackground("green");
            getSideBarView().setWin(isWhite);
        });
        isCheckMate = true;
    }

    @Override
    public void onDrawAttempt(int typeDraw) {
        Platform.runLater(() -> {
            getBoardView().setColorBackground("orange");
            getSideBarView().setDraw(typeDraw);
        });
        isCheckMate = true;
    }

    @Override
    public void onCopyPgnAttempt() {
        GameLogic gameLogic = (GameLogic)getChessLogic();
        String pgn = gameLogic.getPgn();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(pgn);
        clipboard.setContent(content);
    }

    public static class CheckMateEvent extends Event {
        private final boolean isWhite;

        public CheckMateEvent(boolean isWhite) {
            this.isWhite = isWhite;
        }

        public boolean isWhite() {
            return isWhite;
        }
    }

    public static class DrawEvent extends Event {
        private final int typeDraw;

        public DrawEvent(int typeDraw) {
            this.typeDraw = typeDraw;
        }

        public int getTypeDraw() {
            return typeDraw;
        }
    }

    public static class CopyPgnEvent extends Event { }
}
