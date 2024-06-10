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
    public Button buttonApply;
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
                imageViewPauseTime, imageViewSuccessNotificationPeriod);
        setImage(listImageView);

        Hashtable<TextField, ImageView> textFields = new Hashtable<>();
        textFields.put(PORT, imageViewPORT);
        textFields.put(sessionPreparationTime, imageViewSessionPreparationTime);
        textFields.put(sessionDurationLimit, imageViewSessionDurationLimit);
        textFields.put(pauseTime, imageViewPauseTime);
        textFields.put(successNotificationPeriod, imageViewSuccessNotificationPeriod);

        for (TextField textField : textFields.keySet()) {
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!isValid(newValue)) {
                    setWarning(textField, textFields.get(textField));
                } else {
                    removeWarning(textField, textFields.get(textField));
                }
            });
        }

        playersNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidPlayers(newValue)) {
                setWarning(playersNumber, imageViewPlayersNumber);
            } else {
                removeWarning(playersNumber, imageViewPlayersNumber);
            }
        });

        numberCharacters.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValid(newValue)) {
                buttonApply.setVisible(false);
                setWarning(numberCharacters, imageViewNumberCharacters);
            } else {
                buttonApply.setVisible(true);
                removeWarning(numberCharacters, imageViewNumberCharacters);
            }
        });

        hiddenWord.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidWord(newValue)) {
                buttonApply.setVisible(false);
                setWarning(hiddenWord, imageViewHiddenWord);
            } else {
                buttonApply.setVisible(true);
                removeWarning(hiddenWord, imageViewHiddenWord);
            }
        });

        BooleanBinding allFieldsValid = Bindings.createBooleanBinding(() ->
                isValid(PORT.getText()) &&
                isValidPlayers(playersNumber.getText()) &&
                isValid(sessionPreparationTime.getText()) &&
                isValid(sessionDurationLimit.getText()) &&
                isValid(pauseTime.getText()) &&
                isValid(successNotificationPeriod.getText()),
                PORT.textProperty(),
                playersNumber.textProperty(),
                sessionPreparationTime.textProperty(),
                sessionDurationLimit.textProperty(),
                pauseTime.textProperty(),
                successNotificationPeriod.textProperty()
                );

        startServer.disableProperty().bind(allFieldsValid.not());

        checkBoxNumberCharacters.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (checkBoxNumberCharacters.isSelected()) {
                if (!isValid(numberCharacters.getText())) {
                    buttonApply.setVisible(false);
                    setWarning(numberCharacters, imageViewNumberCharacters);
                } else {
                    buttonApply.setVisible(true);
                    removeWarning(numberCharacters, imageViewNumberCharacters);
                }
                checkBoxHiddenWord.setSelected(false);
                hiddenWord.setTooltip(null);
                hiddenWord.setStyle(null);
                hiddenWord.setDisable(true);
                imageViewHiddenWord.setImage(null);

                numberCharacters.setDisable(false);
            }
            else {
                imageViewNumberCharacters.setImage(null);
                numberCharacters.setTooltip(null);
                numberCharacters.setStyle(null);
                numberCharacters.setDisable(true);
                buttonApply.setVisible(false);
            }
        });

        checkBoxHiddenWord.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (checkBoxHiddenWord.isSelected()) {
                if (!isValidWord(hiddenWord.getText())) {
                    buttonApply.setVisible(false);
                    setWarning(hiddenWord, imageViewHiddenWord);
                    hiddenWord.setTooltip(new Tooltip("В поле ввода должно быть слово"));
                } else {
                    buttonApply.setVisible(true);
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
                buttonApply.setVisible(false);
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
                actionButtonApply();
                server.start();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setOnCloseRequest((e) -> {
                    Platform.setImplicitExit(false);
                    if (server != null) {
                        server.stopServer();
                        if (server.isAlive()) {
                            server.interrupt();
                        }
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
            server.stopServer();
            server = null;
            serverCondition.setText("Сервер остановлен");
        }
    }

    @FXML
    protected void actionButtonApply() {
        if (server != null) {
            if (checkBoxNumberCharacters.isSelected()) {
                String number = numberCharacters.getText();
                server.setLimit(number);
            } else if (checkBoxHiddenWord.isSelected()) {
                String word = hiddenWord.getText();
                server.setLimit(word);
            } else {
                server.setLimit("");
            }
        }
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

    private boolean isValidPlayers(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[1-9]{1}\\d*");
    }

    private boolean isValidWord(String string) {
        return string != null && !string.trim().isEmpty() && string.matches("[а-яА-Я]{5,}");
    }
}