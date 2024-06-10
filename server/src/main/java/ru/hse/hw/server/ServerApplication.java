package ru.hse.hw.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApplication extends Application {
    Button startServer;
    Button stopServer;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        startServer = (Button) scene.lookup("#startServer");
        stopServer = (Button) scene.lookup("#stopServer");

        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}