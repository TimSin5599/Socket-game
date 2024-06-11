package ru.hse.hw.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The ServerApplication class, thanks to which the application is launched
 */
public class ServerApplication extends Application {
    /**
     * the function which starts the application
     * @param stage Stage
     * @throws IOException if fxmlLoader failed to load the scene
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * the main method
     * @param args arguments
     */
    public static void main(String[] args) {
        launch();
    }
}