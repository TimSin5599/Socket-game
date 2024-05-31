package ru.hse.hw.client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClientController {
    @FXML
    private Button buttonAbout;
    @FXML
    private Button buttonGame;
    @FXML
    private TextField serverHost;
    @FXML
    private TextField serverPort;
    @FXML
    private TextField playersName;
    @FXML
    private ImageView imageViewServerHost;
    @FXML
    private ImageView imageViewServerPort;
    @FXML
    private ImageView imageViewPlayersName;
    private final Image iconSuccess = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon_success.png")));
    private final Image iconWarning = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon_warning.png")));

    @FXML
    public void initialize() {
        List<ImageView> imageViews = Arrays.asList(imageViewServerHost, imageViewServerPort);
        setImage(imageViews);

        BooleanBinding allFieldsValid = Bindings.createBooleanBinding(() ->
                isValidHost(serverHost.getText()) &&
                isValidNumber(serverPort.getText()) &&
                isValidName(playersName.getText()),
                serverHost.textProperty(),
                serverPort.textProperty(),
                playersName.textProperty()
        );

        serverHost.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidHost(newValue)) {
                setWarning(serverHost, imageViewServerHost);
            } else {
                removeWarning(serverHost, imageViewServerHost);
            }
        });

        serverPort.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidNumber(newValue)) {
                setWarning(serverPort, imageViewServerPort);
            } else {
                removeWarning(serverPort, imageViewServerPort);
            }
        });

        playersName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidName(newValue)) {
                setWarning(playersName, imageViewPlayersName);
            } else {
                removeWarning(playersName, imageViewPlayersName);
            }
        });

        buttonGame.disableProperty().bind(allFieldsValid.not());
    }

    @FXML
    private void startGame(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ru/hse/hw/client/game.fxml"));
        Scene sceneGame = new Scene(fxmlLoader.load(), 700, 500);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(sceneGame);

        stage.setOnCloseRequest(e -> Platform.setImplicitExit(false));

        ClientConnection connection = new ClientConnection(serverHost.getText(), Integer.parseInt(serverPort.getText()), playersName.getText(), fxmlLoader.getController());
        Thread thread = new Thread(connection);
        thread.start();
    }

    private void setImage(List<ImageView> list) {
        for (ImageView image : list) {
            image.setImage(iconSuccess);
        }
    }

    private boolean isValidNumber(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("\\d+");
    }

    private boolean isValidHost(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[a-z]+");
    }

    private boolean isValidName(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[a-zA-Zа-яА-Я]+\\d*");
    }

    private void setWarning(TextField textField, ImageView imageView) {
        textField.setStyle("-fx-border-color: red;");
        Tooltip tooltip = new Tooltip("В поле ввода некорректное значение");
        textField.setTooltip(tooltip);
        imageView.setImage(iconWarning);
    }

    private void removeWarning(TextField textField, ImageView imageView) {
        textField.setStyle(null);
        textField.setTooltip(null);
        imageView.setImage(iconSuccess);
    }
}