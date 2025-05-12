package code.chess.controller.listener;

import code.chess.model.chessgame.Field;

public interface PromotionPawnListener {
    void onPromotionAttempt(Field currentField);
}
