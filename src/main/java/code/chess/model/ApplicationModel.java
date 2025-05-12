package code.chess.model;

import code.chess.model.chessgame.ChessLogic;
import code.chess.model.database.Database;

public class ApplicationModel {
    private ChessLogic chessLogic;

    public ApplicationModel() {
        Database.initialize();
    }

    public ChessLogic getChessLogic() {
        return chessLogic;
    }

    public void setChessLogic(ChessLogic chessLogic) {
        this.chessLogic = chessLogic;
    }
}
