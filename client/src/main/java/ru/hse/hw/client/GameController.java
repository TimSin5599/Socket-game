package ru.hse.hw.client;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.Objects;


public class GameController {
    @FXML
    private Label timer;
    @FXML
    private ListView<String> listView;

    public void initialize() {
        /*timer.textProperty().addListener((observable, oldValue, newValue) -> {

        })*/
    }

    public void updatePlayers(ObservableList<String> players) {
        for (String player : players) {
            System.out.println(player);
        }
        listView.setItems(players);
    }

    public void updatePreparationTime(String time) {
        if (Objects.equals(time, "Время истекло")) {
            timer.setVisible(false);
        } else {
            timer.setText(time);
        }
    }
}
