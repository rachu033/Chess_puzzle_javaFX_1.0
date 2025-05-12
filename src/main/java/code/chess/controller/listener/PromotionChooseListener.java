package code.chess.controller.listener;

import code.chess.model.chessgame.Field;
import code.chess.view.PieceView;

public interface PromotionChooseListener {
    void onChooseAttempt(PieceView pieceView);
}
