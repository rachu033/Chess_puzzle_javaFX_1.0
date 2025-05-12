package code.chess.model.chessgame;

import code.chess.controller.GameController;
import code.chess.model.chessgame.figure.*;

public class GameLogic extends ChessLogic {
    private String pgn = "";

    public GameLogic() {
        super();
        getBoard().startPosition();
    }

    public String getPgn() {
        return pgn;
    }

    @Override
    public boolean tryMove(int currentCol, int currentRow, int targetCol, int targetRow) {
        if(super.tryMove(currentCol, currentRow, targetCol, targetRow)) {
            if(pgn.isEmpty()) {
                pgn = generateMovePGN(currentCol, currentRow, targetCol, targetRow);
            }
            else {
                pgn = pgn + " " + generateMovePGN(currentCol, currentRow, targetCol, targetRow);
            }
            isCheckMateOrDraw(isWhiteTure());
            return true;
        }
        return false;
    }

    @Override
    public void promotePawn(String name, boolean isWhite) {
        pgn = pgn + "=" + name;
        super.promotePawn(name, isWhite);
    }


    private String generateMovePGN(int currentCol, int currentRow, int targetCol, int targetRow) {
        StringBuilder move = new StringBuilder();

        Piece piece = getBoard().getPieceOnField(targetCol, targetRow);

        switch (piece) {
            case Rook ignored -> move.append("R");
            case Knight ignored -> move.append("N");
            case Bishop ignored -> move.append("B");
            case Queen ignored -> move.append("Q");
            case King ignored -> move.append("K");
            case null -> {
                if (targetCol == 0) move.append("0-0-0");
                else move.append("0-0");
                return move.toString();
            }
            default -> {
            }
        }

        move.append(Field.getAlgebraicNotation(currentCol, currentRow));
        move.append(Field.getAlgebraicNotation(targetCol, targetRow));

        return move.toString();
    }

    private void isCheckMateOrDraw(boolean isWhiteTure) {
        if (super.isCheck(isWhiteTure)) {
            if(isAllMoveWrong(isWhiteTure)) {
                getEventHandler().accept(new GameController.CheckMateEvent(!isWhiteTure));
            }
        }
        else if(isAllMoveWrong(isWhiteTure)) {
            getEventHandler().accept(new GameController.DrawEvent(0));
        }
    }

    public boolean isAllMoveWrong(boolean isWhiteTure) {
        Piece[] pieces = isWhiteTure ? getBoard().getWhitePieces() : getBoard().getBlackPieces();
        for (Piece piece : pieces) {
            if (piece.isCaptured()) continue;
            Field currentField = piece.getField();
            int currentCol = currentField.getX();
            int currentRow = currentField.getY();

            for (int col = 0; col < 8; col++) {
                for (int row = 0; row < 8; row++) {
                    if (piece.isValidMove(col, row)) {
                        if (piece instanceof Pawn) {
                            if (!super.isPathToFieldForPawn(currentCol, currentRow, col, row)) continue;
                        } else if (!(piece instanceof Knight)) {
                            if (!super.isPathToField(currentCol, currentRow, col, row)) continue;
                        }

                        if (getBoard().getPieceOnField(col, row) != null && getBoard().getPieceOnField(col, row).isWhite() == piece.isWhite()) continue;

                        if (super.isMoveSafe(piece, col, row)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
