package code.chess.model.chessgame;

import code.chess.model.chessgame.figure.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Board {
    private static final Log logger = LogFactory.getLog(Board.class);
    private final Field[][] fields;
    private final Piece[] whitePieces;
    private final Piece[] blackPieces;

    Board() {
        whitePieces = new Piece[16];
        blackPieces = new Piece[16];
        fields = new Field[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isWhite = (row + col) % 2 == 0;
                Field field = new Field(isWhite, col, row);
                fields[col][row] = field;
            }
        }
        startPosition();
    }

    public void startPosition() {
        String[] whiteBackRow = {"a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"};
        String[] whitePawnsRow = {"a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2"};

        String[] blackBackRow = {"a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"};
        String[] blackPawnsRow = {"a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7"};

        setupPieces(whitePieces, whiteBackRow, whitePawnsRow, true);
        setupPieces(blackPieces, blackBackRow, blackPawnsRow, false);
    }

    private void setupPieces(Piece[] pieces, String[] backRow, String[] pawnRow, boolean isWhite) {
        Class<?>[] pieceTypes = {Rook.class, Knight.class, Bishop.class, Queen.class, King.class, Bishop.class, Knight.class, Rook.class};

        for (int i = 0; i < 8; i++) {
            try {
                pieces[i] = (Piece) pieceTypes[i].getConstructor(boolean.class, Field.class).newInstance(isWhite, fields[Field.getX(backRow[i])][Field.getY(backRow[i])]);
            } catch (Exception e) {
                logger.error("Error setup pieces on board: ", e);
            }
        }

        for (int i = 0; i < 8; i++) {
            pieces[8 + i] = new Pawn(isWhite, fields[Field.getX(pawnRow[i])][Field.getY(pawnRow[i])]);
        }
    }

    public Piece getPieceOnField(int col, int row) {
        return fields[col][row].getPiece();
    }

    public Field getField(int col, int row) {
        return fields[col][row];
    }


    public Piece[] getWhitePieces() {
        return whitePieces;
    }

    public Piece[] getBlackPieces() {
        return blackPieces;
    }

    public String getCurrentPositionString() {
        StringBuilder sb = new StringBuilder();
        appendPiecesPosition(sb, whitePieces, true);
        appendPiecesPosition(sb, blackPieces, false);
        return sb.toString().trim();
    }

    private void appendPiecesPosition(StringBuilder sb, Piece[] pieces, boolean isWhite) {
        for (Piece piece : pieces) {
            if (piece != null && piece.getField() != null) {
                char colorChar = isWhite ? 'W' : 'B';
                char pieceChar = getPieceChar(piece);
                String position = piece.getField().getAlgebraicNotation();
                sb.append(colorChar).append(pieceChar).append(position.toUpperCase()).append(" ");
            }
        }
    }

    private char getPieceChar(Piece piece) {
        if (piece instanceof Pawn) return 'P';
        if (piece instanceof Rook) return 'R';
        if (piece instanceof Knight) return 'N';
        if (piece instanceof Bishop) return 'B';
        if (piece instanceof Queen) return 'Q';
        if (piece instanceof King) return 'K';
        return '?';
    }
}