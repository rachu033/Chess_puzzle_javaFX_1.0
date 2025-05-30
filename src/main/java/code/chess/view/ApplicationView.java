package code.chess.view;

import code.chess.controller.SettingsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ApplicationView {

    private BoardView boardView;
    private SideBarView sideBarView;

    public ApplicationView(Stage primaryStage) {
        initializeUI(primaryStage);
    }

    private void initializeUI(Stage stage) {
        int MARGIN_BOARD_SIZE = 20;
        int CELL_SIZE = 110;
        int SIDEBAR_WIDTH = 200;
        int GRID_SIZE = 8;

        stage.setResizable(false);
        BorderPane borderPane = new BorderPane();

        boardView = new BoardView(CELL_SIZE, MARGIN_BOARD_SIZE, GRID_SIZE);
        sideBarView = new SideBarView(SIDEBAR_WIDTH);

        borderPane.setCenter(boardView);
        borderPane.setRight(sideBarView);

        Scene scene = new Scene(borderPane, GRID_SIZE * CELL_SIZE + SIDEBAR_WIDTH + 2 * MARGIN_BOARD_SIZE, GRID_SIZE * CELL_SIZE + 2 * MARGIN_BOARD_SIZE);

        stage.setTitle("Adam Rachuba WCY22IJ3S1 - Zadania szachowe");
        stage.setScene(scene);
        stage.show();
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public SideBarView getSideBarView() {
        return sideBarView;
    }

    public void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if(fxmlPath.contains("settings")) {
                SettingsController controller = loader.getController();
                controller.setBoardView(getBoardView());
            }
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
