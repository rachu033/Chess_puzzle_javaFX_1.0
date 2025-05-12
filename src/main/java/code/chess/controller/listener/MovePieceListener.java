package code.chess.controller.listener;

import code.chess.view.FieldView;

public interface MovePieceListener {
    void onMoveAttempt(FieldView currentField, FieldView targetField);
}
