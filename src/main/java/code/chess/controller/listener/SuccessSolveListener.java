package code.chess.controller.listener;

import code.chess.model.puzzle.Puzzle;

public interface SuccessSolveListener {
    void onSuccessAttempt(Puzzle puzzle);
}
