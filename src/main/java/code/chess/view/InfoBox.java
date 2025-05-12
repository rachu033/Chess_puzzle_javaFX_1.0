package code.chess.view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class InfoBox extends HBox {
    private final Label infoLabel = new Label();

    public InfoBox() {
        this.setPrefHeight(50);
        this.setStyle("-fx-background-color: gray; -fx-alignment: center; -fx-background-radius: 0;");
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        this.getChildren().add(infoLabel);
    }

    public void setInfo(String text, String backgroundColor, String textColor) {
        this.setStyle("-fx-background-color: " + backgroundColor + ";" +
                "-fx-alignment: center;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 0;");
        infoLabel.setText(text);
        infoLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
    }

    public void setDefaultInfo(String text) {
        setInfo(text, "gray", "white");
    }
}