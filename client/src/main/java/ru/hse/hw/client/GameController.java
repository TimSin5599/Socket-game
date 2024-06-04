package ru.hse.hw.client;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSOutput;

import java.util.Objects;


public class GameController {
    @FXML
    public Button buttonSend;
    @FXML
    private Label timer;
    @FXML
    private ListView<String> listView;
    @FXML
    private HBox hBox;
    ClientConnection connection;
    int[] valuesCharacters;


    public void initialize() {
        /*timer.textProperty().addListener((observable, oldValue, newValue) -> {

        })*/
    }

    void updatePlayers(ObservableList<String> players) {
        /*for (String player : players) {
            System.out.println(player);
        }*/
        listView.setItems(players);
    }

    void updatePreparationTime(String time) {
        if (Objects.equals(time, "Время истекло")) {
            timer.setVisible(false);
        } else {
            timer.setText(time);
        }
    }

    void showWord(int word) {
        hBox.getChildren().clear();
        valuesCharacters = new int[word];

        for (int i = 0; i < word; i++) {
            TextField text = new TextField();
            text.setStyle("-fx-background-color: grey; -fx-font-size: 20;");
            text.setPrefHeight(40);
            text.setPrefWidth(40);
            text.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    for (Node textField : hBox.getChildren()) {
                        if (text != textField) {
                            textField.setDisable(true);
                        }
                    }
                }
            });
            hBox.getChildren().add(text);
        }
        System.out.println(hBox.getChildren());
    }

    @FXML
    void handleSend(ActionEvent event) {
        ObservableList<Node> characters = hBox.getChildren();
        for (int i = 0; i < characters.size(); i++) {
            TextField textField = (TextField) characters.get(i);
            if (!characters.get(i).isDisable()) {
                System.out.println(i);
                connection.requestOnCheckCharacter(textField.getText(), i);
                break;
            }
        }
    }

    void updateWord(int value, int pos) {
        ObservableList<Node> characters = hBox.getChildren();
        switch (value) {
            case -1:
                valuesCharacters[pos] = -1;
                break;
            case 0:
                valuesCharacters[pos] = 0;
                break;
            case 1:
                valuesCharacters[pos] = 1;
                TextField textField = (TextField) characters.get(pos);
                textField.setStyle("-fx-background-color: green; -fx-font-size: 20;");
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
        ObservableList<Node> list = hBox.getChildren();
        for (int i = 0; i < list.size(); i++) {
            if (valuesCharacters[i] != 1) {
                list.get(i).setDisable(false);
            }
        }
    }

    void hideCharacters() {
        ObservableList<Node> list = hBox.getChildren();
        for (Node elem : list) {
            elem.setDisable(true);
        }
    }

    void setConnection(ClientConnection connection) {
        this.connection = connection;
    }
}
