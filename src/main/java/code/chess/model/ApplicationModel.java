package code.chess.model;

import code.chess.model.chessgame.LogicChess;
import code.chess.model.database.Database;

public class ApplicationModel {
    private LogicChess logicChess;

    public ApplicationModel() {
        Database.initialize();
    }

    public LogicChess getChessLogic() {
        return logicChess;
    }

    public void setChessLogic(LogicChess logicChess) {
        this.logicChess = logicChess;
    }
}
