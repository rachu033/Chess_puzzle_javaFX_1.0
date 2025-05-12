package code.chess.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Objects;

public class PieceView extends ImageView {
    private final boolean isWhite;
    private final String name;
    private static int CELL_SIZE;

    public PieceView(boolean isWhite, String typePiece, int CELL_SIZE) {
        this.isWhite = isWhite;
        PieceView.CELL_SIZE = CELL_SIZE;
        String imageFolderPath = "/code/chess/images/";
        String imageName;
        switch (typePiece) {
            case "K" -> {
                imageName = this.isWhite ? "white_king" : "black_king";
                name = "K";
            }
            case "Q" -> {
                imageName = this.isWhite ? "white_queen" : "black_queen";
                name = "Q";
            }
            case "R" -> {
                imageName = this.isWhite ? "white_rook" : "black_rook";
                name = "R";
            }
            case "B" -> {
                imageName = this.isWhite ? "white_bishop" : "black_bishop";
                name = "B";
            }
            case "N" -> {
                imageName = this.isWhite ? "white_knight" : "black_knight";
                name = "N";
            }
            case null, default -> {
                imageName = this.isWhite ? "white_pawn" : "black_pawn";
                name = "P";
            }
        }
        String imagePath = imageFolderPath + imageName + ".png";
        InputStream stream = getClass().getResourceAsStream(imagePath);
        Objects.requireNonNull(stream, "Image not found: " + imagePath);
        this.setImage(new Image(stream));

        this.setFitWidth(CELL_SIZE);
        this.setFitHeight(CELL_SIZE);
    }

    public boolean isWhite() {
        return isWhite;
    }

    public String getName() {
        return name;
    }

    public void upSize() {
        this.setFitWidth(CELL_SIZE);
        this.setFitHeight(CELL_SIZE);
    }

    public void downSize() {
        this.setFitWidth(0.9 * CELL_SIZE);
        this.setFitHeight(0.9 * CELL_SIZE);
    }
}
