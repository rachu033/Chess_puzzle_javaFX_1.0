package code.chess.controller;

import code.chess.view.BoardView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SettingsController {

    @FXML
    private ToggleGroup color;

    @FXML
    private Rectangle lightSquare;

    @FXML
    private Rectangle darkSquare;

    @FXML
    private CheckBox showMovesCheckBox;

    @FXML
    private CheckBox autoLoadPuzzleCheckBox;

    private final File configFile = new File("settings.properties");

    private BoardView boardView;

    @FXML
    public void initialize() {
        color.selectedToggleProperty().addListener((_, _, newToggle) -> {
            if (newToggle != null) {
                String palette = ((RadioButton) newToggle).getText();
                updateColors(palette);
                saveSettings(palette);
            }
        });

        showMovesCheckBox.selectedProperty().addListener((_, _, _) -> saveSettings(null));

        autoLoadPuzzleCheckBox.selectedProperty().addListener((_, _, _) -> saveSettings(null));

        try {
            Configurations configs = new Configurations();
            PropertiesConfiguration config = configs.properties(configFile);

            String savedPalette = config.getString("palette", "Standard");
            for (Toggle toggle : color.getToggles()) {
                RadioButton btn = (RadioButton) toggle;
                if (btn.getText().equals(savedPalette)) {
                    btn.setSelected(true);
                    updateColors(savedPalette);
                    break;
                }
            }

            showMovesCheckBox.setSelected(config.getBoolean("showMoves", true));
            autoLoadPuzzleCheckBox.setSelected(config.getBoolean("autoLoadPuzzle", false));

        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void setBoardView(BoardView boardView) {
        this.boardView = boardView;
    }

    private void updateColors(String palette) {
        Color light, dark;
        dark = switch (palette) {
            case "Chess.com" -> {
                light = Color.web("#eeeeee");
                yield Color.web("#769656");
            }
            case "Lichess" -> {
                light = Color.web("#dee3e6");
                yield Color.web("#8ca2ad");
            }
            default -> {
                light = Color.BEIGE;
                yield Color.BROWN;
            }
        };

        lightSquare.setFill(light);
        darkSquare.setFill(dark);
    }

    private void saveSettings(String palette) {
        try (FileWriter writer = new FileWriter(configFile)) {
            if(palette != null) {
                String currentPalette = ((RadioButton) color.getSelectedToggle()).getText();
                String lightHex = toHex((Color) lightSquare.getFill());
                String darkHex = toHex((Color) darkSquare.getFill());
                if (boardView != null) {
                    Platform.runLater(() -> boardView.updateFieldColors(Color.web(lightHex), Color.web(darkHex)));
                }
                writer.write("palette=" + currentPalette + "\n");
                writer.write("lightSquare=" + lightHex + "\n");
                writer.write("darkSquare=" + darkHex + "\n");
            }
            writer.write("showMoves=" + showMovesCheckBox.isSelected() + "\n");
            writer.write("autoLoadPuzzle=" + autoLoadPuzzleCheckBox.isSelected() + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}
