package ru.hse.hw.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class GameController {
    @FXML
    private Button buttonSend;
    @FXML
    public Label firstLabel;
    @FXML
    public ListView<String> listPlayersMoves;
    @FXML
    private Label session_ID;
    public Button buttonExit;
    @FXML
    private Label timer;
    @FXML
    private ListView<VBox> listView;
    @FXML
    private FlowPane flowPane;
    private ClientConnection connection;
    private int[] valuesCharacters;

    void updatePlayers(ObservableList<String> players) {
        ObservableList<VBox> list = FXCollections.observableArrayList();

        for (VBox vBox : listView.getItems()) {
            ObservableList<Node> elemVbox = vBox.getChildren();
            if (players.contains(((Label) elemVbox.getFirst()).getText())) {
                list.add(vBox);
                players.remove(((Label) elemVbox.getFirst()).getText());
            }
        }

        for (String player : players) {
            VBox vBox = new VBox();
            vBox.setStyle("-fx-alignment: center");
            Label label = new Label(player);
            label.setStyle("-fx-font-size: 14;");
            vBox.getChildren().add(label);
            list.add(vBox);
        }

        listView.setItems(list);
        /*for (int i = 0; i < players.size(); i++) {
            VBox vBox = new VBox();
            vBox.setStyle("-fx-alignment: center");
            Label label = new Label(players.get(i));
            label.setStyle("-fx-font-size: 14");
            vBox.getChildren().add(label);
            list.add(vBox);
        }
        listView.setItems(list);*/
    }

    void updatePreparationTime(String time) {
        if (Objects.equals(time, "Время истекло")) {
            timer.setVisible(false);
        } else {
            timer.setText(time);
        }
    }

    void updateSessionTime(String time) {
        timer.setText(time);
    }

    void showWord(int word) {
        flowPane.getChildren().clear();
        valuesCharacters = new int[word];

        ObservableList<VBox> list = FXCollections.observableArrayList();
        for (VBox vbox : listView.getItems()) {
            FlowPane flowPane = new FlowPane();
            flowPane.setStyle("-fx-alignment: center; -fx-hgap: 2; -fx-vgap: 2");
            for (int i = 0; i < word; i++) {
                Label label = new Label();
                label.setStyle("-fx-background-color: grey; -fx-pref-height: 10; -fx-pref-width: 20");
                flowPane.getChildren().add(label);
            }
            vbox.getChildren().add(flowPane);
            list.add(vbox);
        }
        listView.setItems(list);

        for (int i = 0; i < word; i++) {
            TextField text = new TextField();
            text.setStyle("-fx-background-color: grey; -fx-font-size: 20; -fx-alignment: center");
            text.setPrefHeight(40);
            text.setPrefWidth(45);
            text.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    ObservableList<Node> children = flowPane.getChildren();
                    for (int j = 0; j < valuesCharacters.length; j++) {
                        children.get(j).setDisable(valuesCharacters[j] == 1);
                    }
                }
                else {
                    if (newValue.length() > 1) {
                        String string = text.getText();
                        text.setText(String.valueOf(string.charAt(0)));
                    }
                    for (Node textField : flowPane.getChildren()) {
                        textField.setDisable(text != textField);
                    }
                }
            });
            flowPane.getChildren().add(text);
        }
        System.out.println(flowPane.getChildren());
    }

    @FXML
    void handleSend() {
        ObservableList<Node> characters = flowPane.getChildren();
        for (int i = 0; i < characters.size(); i++) {
            TextField textField = (TextField) characters.get(i);
            if (!characters.get(i).isDisable()) {
                connection.requestOnCheckCharacter(textField.getText(), i);
                break;
            }
        }
    }

    synchronized void updateWord(int value, int pos) {
        ObservableList<Node> characters = flowPane.getChildren();
        System.out.println(value + " " + pos);
        switch (value) {
            case -1:
                valuesCharacters[pos] = -1;
                TextField textField1 = (TextField) characters.get(pos);
                textField1.setText("");
                break;
            case 0:
                valuesCharacters[pos] = 0;
                TextField textField2 = (TextField) characters.get(pos);
                textField2.setText("");
                break;
            case 1:
                valuesCharacters[pos] = 1;
                TextField textField3 = (TextField) characters.get(pos);
                textField3.setStyle("-fx-background-color: #71F668; -fx-font-size: 20; -fx-opacity: 0.9; -fx-font-weight: bold; -fx-alignment: center;");
                break;
            default:
                break;
        }

        for (int i = 0; i < valuesCharacters.length; i++) {
            TextField textField = (TextField) characters.get(i);
            if (valuesCharacters[i] != 1) {
                textField.setDisable(false);
            } else {
                textField.setDisable(true);
            }
        }
    }

    void openCharacters() {
        ObservableList<Node> list = flowPane.getChildren();
        for (int i = 0; i < list.size(); i++) {
            if (valuesCharacters[i] != 1) {
                list.get(i).setDisable(false);
            }
        }
    }

    void hideCharacters() {
        ObservableList<Node> list = flowPane.getChildren();
        for (Node elem : list) {
            elem.setDisable(true);
        }
    }

    void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    void updatePlayersMoves(String string) {
        ObservableList<String> list = listPlayersMoves.getItems();
        list.add(string);
        listPlayersMoves.setItems(list);
    }

    synchronized void showWinner(String nameWinner) {
        ObservableList<Node> list = flowPane.getChildren();
        list.clear();
        Label label = new Label("Игра закончена! Победитель - " + nameWinner);
        label.setDisable(false);
        label.setStyle("-fx-font-size: 20;");
        timer.setText("");
        buttonExit.setVisible(true);
        list.add(label);
    }

    void setSessionID(int session_ID) {
        this.session_ID.setText(String.valueOf(session_ID));
    }

    @FXML
    void handleExit(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ru/hse/hw/client/client.fxml"));

        try {
            Scene sceneGame = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(sceneGame);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void updateLetterPlayer(String playerName, int position) {
        for (VBox vBox : listView.getItems()) {
            ObservableList<Node> list = vBox.getChildren();
            if (Objects.equals(((Label) list.getFirst()).getText(), playerName)) {
                FlowPane flowPane = (FlowPane) list.get(1);
                ObservableList<Node> listLabels = flowPane.getChildren();
                listLabels.get(position).setStyle("-fx-background-color: #71F668; -fx-pref-height: 10; -fx-pref-width: 20");
            }
        }
    }

    void stopGame() {
        flowPane.getChildren().clear();
        Label label = new Label("Сеанс игры прерван");
        label.setStyle("-fx-font-size: 20");
        flowPane.getChildren().add(label);
        buttonExit.setVisible(true);
    }

    FlowPane getFlowPane() {
        return flowPane;
    }
}
