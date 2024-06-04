package ru.hse.hw.server;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;
import java.util.List;

public class ServerController {
    @FXML
    public ImageView imageViewPORT;
    @FXML
    public ImageView imageViewPlayersNumber;
    @FXML
    public ImageView imageViewSessionPreparationTime;
    @FXML
    public ImageView imageViewSessionDurationLimit;
    @FXML
    public ImageView imageViewPauseTime;
    @FXML
    public ImageView imageViewSuccessNotificationPeriod;
    @FXML
    public CheckBox checkBoxNumberCharacters;
    @FXML
    public CheckBox checkBoxHiddenWord;
    @FXML
    public ImageView imageViewNumberCharacters;
    @FXML
    public ImageView imageViewHiddenWord;
    @FXML
    public Label serverCondition;
    @FXML
    private TextField PORT;
    @FXML
    private TextField playersNumber;
    @FXML
    private TextField sessionPreparationTime;
    @FXML
    private TextField sessionDurationLimit;
    @FXML
    private TextField pauseTime;
    @FXML
    private TextField successNotificationPeriod;
    @FXML
    private TextField numberCharacters;
    @FXML
    private TextField hiddenWord;
    @FXML
    private Button startServer;
    @FXML
    private Button stopServer;
    private Server server;
    private final Image imageSuccess = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon_success.png")));
    private final Image imageWarning = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon_warning.png")));

    @FXML
    public void initialize() {
        List<ImageView> listImageView = Arrays.asList(imageViewPORT, imageViewPlayersNumber, imageViewSessionPreparationTime, imageViewSessionDurationLimit,
                imageViewPauseTime, imageViewSuccessNotificationPeriod, imageViewNumberCharacters);
        setImage(listImageView);
        hiddenWord.setDisable(true);

        Hashtable<TextField, ImageView> textFields = new Hashtable<>();
        textFields.put(PORT, imageViewPORT);
        textFields.put(playersNumber, imageViewPlayersNumber);
        textFields.put(sessionPreparationTime, imageViewSessionPreparationTime);
        textFields.put(sessionDurationLimit, imageViewSessionDurationLimit);
        textFields.put(pauseTime, imageViewPauseTime);
        textFields.put(successNotificationPeriod, imageViewSuccessNotificationPeriod);
        textFields.put(numberCharacters, imageViewNumberCharacters);

        for (TextField textField : textFields.keySet()) {
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!isValid(newValue)) {
                    setWarning(textField, textFields.get(textField));
                } else {
                    removeWarning(textField, textFields.get(textField));
                }
            });
        }

        hiddenWord.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidWord(newValue)) {
                setWarning(hiddenWord, imageViewHiddenWord);
            } else {
                removeWarning(hiddenWord, imageViewHiddenWord);
            }
        });

        BooleanBinding allFieldsValid = Bindings.createBooleanBinding(() ->
                isValid(PORT.getText()) &&
                isValid(playersNumber.getText()) &&
                isValid(sessionPreparationTime.getText()) &&
                isValid(sessionDurationLimit.getText()) &&
                isValid(pauseTime.getText()) &&
                isValid(successNotificationPeriod.getText()) &&
                (!checkBoxNumberCharacters.isSelected() || isValid(numberCharacters.getText())) &&
                (!checkBoxHiddenWord.isSelected() || isValidWord(hiddenWord.getText())) &&
                (checkBoxHiddenWord.isSelected() || checkBoxNumberCharacters.isSelected()),
                PORT.textProperty(),
                playersNumber.textProperty(),
                sessionPreparationTime.textProperty(),
                sessionDurationLimit.textProperty(),
                pauseTime.textProperty(),
                successNotificationPeriod.textProperty(),
                hiddenWord.textProperty(),
                numberCharacters.textProperty(),
                checkBoxHiddenWord.selectedProperty(),
                checkBoxNumberCharacters.selectedProperty()
                );

        startServer.disableProperty().bind(allFieldsValid.not());

        checkBoxNumberCharacters.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (checkBoxNumberCharacters.isSelected()) {
                if (!isValid(numberCharacters.getText())) {
                    setWarning(numberCharacters, imageViewNumberCharacters);
                } else {
                    removeWarning(numberCharacters, imageViewNumberCharacters);
                }
                checkBoxHiddenWord.setSelected(false);
                hiddenWord.setTooltip(null);
                hiddenWord.setStyle(null);
                hiddenWord.setDisable(true);
                numberCharacters.setDisable(false);
                imageViewHiddenWord.setImage(null);
            }
            else {
                imageViewNumberCharacters.setImage(null);
                numberCharacters.setTooltip(null);
                numberCharacters.setStyle(null);
                numberCharacters.setDisable(true);
            }
        });

        checkBoxHiddenWord.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (checkBoxHiddenWord.isSelected()) {
                if (!isValidWord(hiddenWord.getText())) {
                    setWarning(hiddenWord, imageViewHiddenWord);
                    hiddenWord.setTooltip(new Tooltip("В поле ввода должно быть слово"));
                } else {
                    removeWarning(hiddenWord, imageViewHiddenWord);
                }
                imageViewNumberCharacters.setImage(null);
                numberCharacters.setTooltip(null);
                numberCharacters.setStyle(null);
                numberCharacters.setDisable(true);
                checkBoxNumberCharacters.setSelected(false);
                hiddenWord.setDisable(false);
            }
            else {
                imageViewHiddenWord.setImage(null);
                hiddenWord.setTooltip(null);
                hiddenWord.setStyle(null);
                hiddenWord.setDisable(true);
            }
        });
    }

    @FXML
    protected void startServer(ActionEvent event) throws IOException {
        try {
            if (server == null) {
                server = new Server(Integer.parseInt(PORT.getText()), Integer.parseInt(playersNumber.getText()), Integer.parseInt(sessionPreparationTime.getText()),
                        Integer.parseInt(sessionDurationLimit.getText()), Integer.parseInt(pauseTime.getText()), Integer.parseInt(successNotificationPeriod.getText()));
                serverCondition.setText("Сервер запущен");
                Thread thread = new Thread(server);
                thread.start();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setOnCloseRequest((e) -> {
                    Platform.setImplicitExit(false);
                    thread.interrupt();
                    if (server != null) {
                        server.stop();
                    }
                    Platform.exit();
                });
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    @FXML
    protected void stopServer(ActionEvent event) {
        if (server != null) {
            server.stop();
            server = null;
            serverCondition.setText("Сервер остановлен");
        }
    }

    @FXML
    protected void checkBoxAction(ActionEvent event) {

    }

    private void setWarning(TextField textField, ImageView imageView) {
        textField.setStyle("-fx-border-color: red;");
        Tooltip tooltip = new Tooltip("В поле ввода должно быть число");
        textField.setTooltip(tooltip);
        imageView.setImage(imageWarning);
    }

    private void removeWarning(TextField textField, ImageView imageView) {
        textField.setStyle(null);
        textField.setTooltip(null);
        imageView.setImage(imageSuccess);
    }

    private void setImage(List<ImageView> listImageView) {
        for (ImageView imageView : listImageView) {
            imageView.setImage(imageSuccess);
        }
    }

    private boolean isValid(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("\\d+");
    }

    private boolean isValidWord(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[а-яА-Я]+");
    }
}