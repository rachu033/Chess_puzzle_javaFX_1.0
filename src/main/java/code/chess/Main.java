package code.chess;

import code.chess.controller.ApplicationController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        ApplicationController applicationController = new ApplicationController(primaryStage);
        Thread thread = new Thread(applicationController);
        thread.setDaemon(true);
        primaryStage.setOnCloseRequest(_ -> {
            Platform.exit();
            System.exit(0);
        });
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
