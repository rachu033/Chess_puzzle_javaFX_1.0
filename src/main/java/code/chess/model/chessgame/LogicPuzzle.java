package code.chess.model.chessgame;

import code.chess.controller.PuzzleController;
import code.chess.model.chessgame.figure.*;
import code.chess.model.puzzle.Puzzle;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LogicPuzzle extends LogicChess {
    private final Puzzle puzzle;
    private String nextMove;
    private int roundSolution = 0;
    private final boolean isWhitePuzzle;

    private static final Map<Character, Class<? extends Piece>> pieceTypeMap = new HashMap<>();

    static {
        pieceTypeMap.put('K', King.class);
        pieceTypeMap.put('Q', Queen.class);
        pieceTypeMap.put('R', Rook.class);
        pieceTypeMap.put('B', Bishop.class);
        pieceTypeMap.put('N', Knight.class);
    }

    public LogicPuzzle(Puzzle puzzle) {
        super();
        this.puzzle = puzzle;
        List<String> solution = puzzle.getSolution();
        nextMove = solution.get(roundSolution);
        this.isWhitePuzzle = puzzle.getPgn().trim().split("\\s+").length % 2 == 0;
    }

    @Override
    public boolean tryMove(int currentCol, int currentRow, int targetCol, int targetRow) {
        System.out.println(nextMove);
        String current = nextMove.substring(0, 2);
        String target = nextMove.substring(2);

        if (roundSolution % 2 == 0) {
            if (!(currentCol == Field.getX(current) && currentRow == Field.getY(current) && targetCol == Field.getX(target) && targetRow == Field.getY(target))) {
                if(getBoard().getPieceOnField(targetCol, targetRow)==null) {
                    getEventHandler().accept(new PuzzleController.WrongEvent());
                }
                else if(getBoard().getPieceOnField(currentCol, currentRow).isWhite() != getBoard().getPieceOnField(targetCol, targetRow).isWhite()) {
                    getEventHandler().accept(new PuzzleController.WrongEvent());
                }
                return false;
            }
            roundSolution += 1;
            super.tryMove(currentCol, currentRow, targetCol, targetRow);
            List<String> solution = puzzle.getSolution();
            if (roundSolution == solution.size()) {
                getEventHandler().accept(new PuzzleController.SuccessEvent(puzzle));
                return true;
            }
            nextMove = solution.get(roundSolution);
            current = nextMove.substring(0, 2);
            target = nextMove.substring(2);
            Field currentField = getBoard().getField(Field.getX(current), Field.getY(current));
            Field targetField = getBoard().getField(Field.getX(target), Field.getX(target));
            if(nextMove.length() == 5) {
                promotePawn(String.valueOf(nextMove.charAt(4)), isWhitePuzzle);
            }
            getEventHandler().accept(new PuzzleController.MoveSimulationEvent(currentField, targetField));
        } else {
            roundSolution += 1;
            List<String> solution = puzzle.getSolution();
            try {
                Thread.sleep(250);
                super.tryMove(Field.getX(current), Field.getY(current), Field.getX(target), Field.getY(target));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(nextMove.length() == 5) {
                promotePawn(String.valueOf(nextMove.charAt(4)), !isWhitePuzzle);
            }
            nextMove = solution.get(roundSolution);
        }
        return true;
    }

    public void loadFromPgnMoves(String pgnMoves) {
        if (pgnMoves.isEmpty()) return;
        String[] moves = pgnMoves.split(" ");
        for (String move : moves) {
            applyMoveFromNotation(move);
        }
    }

    private void applyMoveFromNotation(String notation) {
        String cleanNotation = notation.replaceAll("[x+#]", "");
        Piece[] pieces = isWhiteTure() ? getBoard().getWhitePieces() : getBoard().getBlackPieces();

        // Castle
        if (cleanNotation.equals("O-O") || cleanNotation.equals("O-O-O")) {
            int rookCol = notation.equals("O-O") ? 7 : 0;

            for (Piece piece : pieces) {
                if (piece instanceof King king) {
                    super.tryMove(king.getField().getX(), king.getField().getY(), rookCol, king.getField().getY());
                    return;
                }
            }
        }

        // Pieces without pawns
        if (Character.isUpperCase(cleanNotation.charAt(0))) {
            char pieceType = cleanNotation.charAt(0);
            var pieceClass = pieceTypeMap.get(pieceType);

            if (pieceClass != null) {
                if (cleanNotation.length() == 3) {
                    int col = Field.getX(cleanNotation.substring(1));
                    int row = Field.getY(cleanNotation.substring(1));
                    for (Piece piece : pieces) {
                        if (pieceClass.isInstance(piece) && piece.isValidMove(col, row)) {
                            if (super.tryMove(piece.getField().getX(), piece.getField().getY(), col, row)) {
                                return;
                            }
                        }
                    }
                } else {
                    char actualPosition = cleanNotation.charAt(1);
                    boolean isCol = (Character.isLetter(actualPosition));
                    int col = Field.getX(cleanNotation.substring(2));
                    int row = Field.getY(cleanNotation.substring(2));
                    for (Piece piece : pieces) {
                        if (pieceClass.isInstance(piece) && ((isCol && actualPosition == (char) ('a' + piece.getField().getX())) || (!isCol && actualPosition == (char) ('8' - piece.getField().getY())))) {
                            if (super.tryMove(piece.getField().getX(), piece.getField().getY(), col, row)) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        // Only pawns
        else {
            if (cleanNotation.length() == 2) {
                int col = Field.getX(cleanNotation);
                int row = Field.getY(cleanNotation);
                for (Piece piece : pieces) {
                    if (piece instanceof Pawn && piece.isValidMove(col, row)) {
                        if (super.tryMove(piece.getField().getX(), piece.getField().getY(), col, row)) {
                            return;
                        }
                    }
                }
            } else if (cleanNotation.length() == 3) {
                char actualCol = cleanNotation.charAt(0);
                int col = Field.getX(cleanNotation.substring(1));
                int row = Field.getY(cleanNotation.substring(1));
                for (Piece piece : pieces) {
                    if (piece instanceof Pawn && piece.isValidMove(col, row) && actualCol == (char) ('a' + piece.getField().getX())) {
                        if (super.tryMove(piece.getField().getX(), piece.getField().getY(), col, row)) {
                            return;
                        }
                    }
                }
            } else if (cleanNotation.length() == 4) {
                String promotion = cleanNotation.substring(3);
                int col = Field.getX(cleanNotation.substring(0, 2));
                int row = Field.getY(cleanNotation.substring(0, 2));
                for (Piece piece : pieces) {
                    if (piece instanceof Pawn && piece.isValidMove(col, row)) {
                        if (super.tryMove(piece.getField().getX(), piece.getField().getY(), col, row)) {
                            promotePawn(promotion, piece.isWhite());
                            return;
                        }
                    }
                }
            } else {
                String promotion = cleanNotation.substring(4);
                char actualCol = cleanNotation.charAt(0);
                int col = Field.getX(cleanNotation.substring(1, 3));
                int row = Field.getY(cleanNotation.substring(1, 3));
                for (Piece piece : pieces) {
                    if (piece instanceof Pawn && piece.isValidMove(col, row) && actualCol == (char) ('a' + piece.getField().getX())) {
                        if (super.tryMove(piece.getField().getX(), piece.getField().getY(), col, row)) {
                            promotePawn(promotion, piece.isWhite());
                            return;
                        }
                    }
                }
            }
        }
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }
}
