package ru.hse.hw.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The GameController class responsible for interaction of the GUI with data from the server
 */
public class GameController {
    @FXML
    public Label firstLabel;
    @FXML
    public TableView<Move> listPlayersMoves;
    @FXML
    public TableColumn<Move, Integer> orderMove;
    @FXML
    public TableColumn<Move, String> letter;
    @FXML
    public TableColumn<Move, Integer> place;
    @FXML
    public TableColumn<Move,Integer> serverResponse;
    @FXML
    public VBox winnerTable;
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

    @FXML
    void initialize() {
        orderMove.setCellValueFactory(
                new PropertyValueFactory<>("orderMove"));
        letter.setCellValueFactory(new PropertyValueFactory<>("letter"));
        place.setCellValueFactory(new PropertyValueFactory<>("place"));
        serverResponse.setCellValueFactory(new PropertyValueFactory<>("serverResponse"));
    }
    /**
     * Function for updating player list in the client window
     * @param players player list
     */
    synchronized void updatePlayers(ObservableList<String> players) {
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
    }

    /**
     * Function for setting the time during preparation period
     * @param time time in seconds
     */
    void updatePreparationTime(String time) {
        timer.setText(time);
    }

    /**
     * Function for setting the time during the game period
     * @param time time in seconds
     */
    void updateSessionTime(String time) {
        timer.setText(time);
    }

    /**
     * Function to set the game panel for guessing letters and set the game panel in the player list
     * to display the current game state of other players
     * @param numberLetters number of letters
     */
    synchronized void showWord(int numberLetters) {
        flowPane.getChildren().clear();
        valuesCharacters = new int[numberLetters];

        ObservableList<VBox> list = FXCollections.observableArrayList();
        for (VBox vbox : listView.getItems()) {
            FlowPane flowPane = new FlowPane();
            flowPane.setStyle("-fx-alignment: center; -fx-hgap: 2; -fx-vgap: 2");
            for (int i = 0; i < numberLetters; i++) {
                Label label = new Label();
                label.setStyle("-fx-background-color: grey; -fx-pref-height: 10; -fx-pref-width: 20");
                flowPane.getChildren().add(label);
            }
            vbox.getChildren().add(flowPane);
            list.add(vbox);
        }
        listView.setItems(list);

        for (int i = 0; i < numberLetters; i++) {
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
    }

    /**
     * Function for processing the "Send" button press
     */
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

    /**
     * Function for updating the guessed letter in a word
     * @param value server response
     * @param pos word position
     */
    synchronized void updateWord(int value, int pos) {
        ObservableList<Node> characters = flowPane.getChildren();
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
            textField.setDisable(valuesCharacters[i] == 1);
        }
    }

    /**
     * Sets a client connection
     * @param connection connection
     */
    void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * Function for adding player's movement to the movements list
     * @param string Movement
     */
    void updatePlayersMoves(String string) {
        String[] array = string.split(" ");
        ObservableList<Move> list = listPlayersMoves.getItems();
        list.add(new Move(Integer.parseInt(array[0]), array[1], Integer.parseInt(array[2]), Integer.parseInt(array[3])));
        listPlayersMoves.setItems(list);
    }

    /**
     * Function for displaying the table with players in according to their results
     * @param listPlayer player list
     */
    synchronized void showTable(String listPlayer) {
        ObservableList<Node> list = flowPane.getChildren();
        list.clear();
        flowPane.setVisible(false);

        ObservableList<Node> winnerList = winnerTable.getChildren();
        winnerList.clear();
        winnerTable.setVisible(true);
        String[] players = listPlayer.split(" ");

        Label label = new Label("Игра закончена!");
        label.setDisable(false);
        label.setStyle("-fx-font-size: 24;");
        winnerList.add(label);

        for (int i = 0; i < players.length; i++) {
            Label player = new Label(i + 1 + ". " + players[i] + "\n");
            player.setDisable(false);
            player.setStyle("-fx-font-size: 20;");
            winnerList.add(player);
        }

        timer.setText("");
        buttonExit.setVisible(true);
    }

    /**
     * Sets the session_ID in the client window
     * @param session_ID session_ID
     */
    void setSessionID(int session_ID) {
        this.session_ID.setText(String.valueOf(session_ID));
    }

    /**
     * Function for processing the "Exit" button press
     * @param event event
     */
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

    /**
     * Function to update the status of a player's letter in the player list
     * @param playerName player name
     * @param position position
     */
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

    /**
     * Game stop function
     */
    void stopGame() {
        flowPane.getChildren().clear();
        Label label = new Label("Сеанс игры прерван");
        label.setStyle("-fx-font-size: 20");
        flowPane.getChildren().add(label);
        buttonExit.setVisible(true);
    }

    /**
     * @return flowPane
     */
    FlowPane getFlowPane() {
        return flowPane;
    }
}
