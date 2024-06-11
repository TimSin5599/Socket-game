package ru.hse.hw.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The ClientApplication class runs a client-side application
 */
public class ClientApplication extends Application {
    /**
     * Start function in the application
     * @param stage stage
     * @throws IOException when scene loading fails
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("client.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Client");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Main function in the application
     * @param args arguments
     */
    public static void main(String[] args) {
        launch();
    }
}