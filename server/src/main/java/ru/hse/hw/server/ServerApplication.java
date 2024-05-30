package ru.hse.hw.server;

import javafx.application.Application;
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
        /*TextField PORT = (TextField) scene.lookup("#PORT");
        TextField playersNumber = (TextField) scene.lookup("#playersNumber");
        TextField sessionPreparationTime = (TextField) scene.lookup("#sessionPreparationTime");
        TextField sessionDurationLimit = (TextField) scene.lookup("#sessionDurationLimit");
        TextField pauseTime = (TextField) scene.lookup("#pauseTime");
        TextField successNotificationPeriod = (TextField) scene.lookup("#successNotificationPeriod");
        TextField numberCharacters = (TextField) scene.lookup("#numberCharacters");
        TextField hiddenWord = (TextField) scene.lookup("#hiddenWord");
        CheckBox checkBoxNumberOfCharacters = (CheckBox) scene.lookup("#checkBoxNumberOfCharacters");
        CheckBox checkBoxHiddenWord = (CheckBox) scene.lookup("#checkBoxHiddenWord");
        ImageView imageViewPORT = (ImageView) scene.lookup("#imageViewPORT");
        ImageView imageViewPlayersNumber = (ImageView) scene.lookup("#imageViewPlayersNumber");
        ImageView imageViewSessionPreparationTime = (ImageView) scene.lookup("#imageViewSessionPreparationTime");
        ImageView imageViewSessionDurationLimit = (ImageView) scene.lookup("#imageViewSessionDurationLimit");
        ImageView imageViewPauseTime = (ImageView) scene.lookup("#imageViewPauseTime");
        ImageView imageViewSuccessNotificationPeriod = (ImageView) scene.lookup("#imageViewSuccessNotificationPeriod");*/
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 500);
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