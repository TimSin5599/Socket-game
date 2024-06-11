package ru.hse.hw.client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    /**
     * Icon for displaying correctness of text fields
     */
    private final Image iconSuccess = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon_success.png")));
    /**
     * Icon for displaying incorrect of text fields
     */
    private final Image iconWarning = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon_warning.png")));

    /**
     * Function for initializing the scene in the client window
     */
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

    /**
     * Function for switching to a game scene in the application
     * @param event event
     */
    @FXML
    private void startGame(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ru/hse/hw/client/game.fxml"));
        GameController gameController = null;
        try {
            Scene sceneGame = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(sceneGame);

            gameController = fxmlLoader.getController();
            ClientConnection connection = new ClientConnection(serverHost.getText(), Integer.parseInt(serverPort.getText()), playersName.getText(), gameController);
            gameController.setConnection(connection);
            connection.start();

            stage.setOnCloseRequest(e -> {
                Platform.setImplicitExit(false);
                connection.interrupt();
                connection.close();
                Platform.exit();
            });
        } catch (IOException e) {
            System.err.println("Failed to connect to server or failed download the scene of game");
            if (gameController != null) {
                ObservableList<Node> list = gameController.getFlowPane().getChildren();
                list.clear();
                Label label = new Label("Не удалось подключится к серверу. Попробуйте повторить попытку позже.");
                label.setStyle("-fx-font-size: 18");
                list.add(label);
                gameController.buttonExit.setVisible(true);
            }
        }
    }

    @FXML
    void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("");
        alert.setContentText("""
                Игра представляет собой угадывание букв в слове, кто первый угадал, тот и выиграл!
                Автор реализации - Синицын Тимофей Сергеевич БПИ-222
                """);
        alert.show();
    }

    /**
     * The function for placing images next to text fields
     * @param list image list
     */
    private void setImage(List<ImageView> list) {
        for (ImageView image : list) {
            image.setImage(iconSuccess);
        }
    }

    /**
     * Function for checking a number for validity
     * @param string a string for checking
     * @return a boolean value which indicates the correctness of the string
     */
    private boolean isValidNumber(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("\\d+");
    }

    /**
     * Function for checking hostname validity
     * @param string a string for checking
     * @return a boolean value which indicates the correctness of the string
     */
    private boolean isValidHost(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[a-z]+");
    }

    /**
     * Function for checking player's name for validity
     * @param string a string for checking
     * @return a boolean value which indicates the correctness of the string
     */
    private boolean isValidName(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[a-zA-Zа-яА-Я]+\\d*");
    }

    /**
     * The function for setting a warning on a text field
     * @param textField text field value to set a warning
     * @param imageView image location to set the wrong image
     */
    private void setWarning(TextField textField, ImageView imageView) {
        textField.setStyle("-fx-border-color: red;");
        Tooltip tooltip = new Tooltip("В поле ввода некорректное значение");
        textField.setTooltip(tooltip);
        imageView.setImage(iconWarning);
    }

    /**
     * The function for deactivating the warning in the text field
     * @param textField text field value to remove a warning
     * @param imageView image location to set the correct image
     */
    private void removeWarning(TextField textField, ImageView imageView) {
        textField.setStyle(null);
        textField.setTooltip(null);
        imageView.setImage(iconSuccess);
    }
}