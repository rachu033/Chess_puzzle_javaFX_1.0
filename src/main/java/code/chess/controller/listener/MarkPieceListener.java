package code.chess.controller.listener;

import code.chess.view.FieldView;

public interface MarkPieceListener {
    void onMarkAttempt(FieldView chooseField);
}
