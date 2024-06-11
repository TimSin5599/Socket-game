package ru.hse.hw.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.io.*;
import java.net.Socket;

/**
 * The ClientConnection class which provides connection and communication with the server
 */
public class ClientConnection extends Thread {
    /**
     * players name
     */
    private final String playersName;
    /**
     * game controller
     */
    private final GameController gameController;
    /**
     * client socket
     */
    private final Socket socket;
    /**
     * Reader for reading data from a socket
     */
    private final BufferedReader reader;
    /**
     * Writer for writing data from a socket
     */
    private final BufferedWriter writer;

    /**
     * ClientConnection builder
     * @param addressServer server host
     * @param port port
     * @param playersName player name
     * @param gameController game controller
     * @throws IOException if an I/ O error occurs when creating the input stream, the socket is closed,
     * the socket is not connected, or the socket input has been shutdown using shutdownInput()
     */
    public ClientConnection(String addressServer, int port, String playersName, GameController gameController) throws IOException {
        this.playersName = playersName;
        this.gameController = gameController;
        socket = new Socket(addressServer, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * The function that writes the player's name at startup and then accepts a category of data from the server
     */
    @Override
    public void run() {
        try (socket; reader; writer) {
            System.out.println("Клиент установил связь с сервером");
            writer.write(playersName);
            writer.newLine();
            writer.flush();

            while (Platform.isImplicitExit()) {
                String category = reader.readLine();
                dataCategory(category);
            }
        } catch (IOException e) {
            System.out.println("Сокет сервера закрыт");
        } finally {
            System.out.println("Сокет и буферы клиента закрыты");
        }
    }

    /**
     * The {@code dataCategory()} function defines the function needed to read the data
     * @param category the category of data to be received by the client
     * @throws IOException if the socket is closed
     */
    private void dataCategory(String category) throws IOException {
        switch (category) {
            case "session_ID":
                initializeSession();
                break;
            case "preparationTime":
                preparationTime();
                break;
            case "pauseTime":
                pauseTime();
                break;
            case "playersList":
                playersList();
                break;
            case "word":
                showWord();
                break;
            case "answerOnRequest":
                answerOnRequest();
                break;
            case "updateLetterPlayer":
                updateLetterPlayer();
                break;
            case "playersMove":
                playersMove();
                break;
            case "currentTimeSession":
                currentTimeSession();
                break;
            case "listPlayers":
                showTable();
                break;
            case "stopGame":
                stopGame();
                break;
            default:
                break;
        }
    }

    /**
     * Function for stopping the game
     * @throws IOException if the socket is closed
     */
    private void stopGame() throws IOException {
        Platform.runLater(gameController::stopGame);
        throw new IOException();
    }

    /**
     * Function to update the state of the player's letter
     * @throws IOException if the socket is closed
     */
    private void updateLetterPlayer() throws IOException {
        try {
            String playerName = reader.readLine();
            int position = Integer.parseInt(reader.readLine());
            Platform.runLater(() -> gameController.updateLetterPlayer(playerName, position));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Function for session initialization in the client window
     * @throws IOException if the socket is closed
     */
    private void initializeSession() throws IOException{
        try {
           int session_ID = Integer.parseInt(reader.readLine());
           Platform.runLater(() -> gameController.setSessionID(session_ID));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Function for changing the label in the client window
     */
    private void pauseTime() {
        Platform.runLater(() -> gameController.firstLabel.setText("Игра скоро начнется!"));
    }

    /**
     * Function to show the table of the player with their current results
     * @throws IOException if the socket is closed
     */
    private void showTable() throws IOException {
        String playerList = reader.readLine();
        Platform.runLater(() -> gameController.showTable(playerList));
    }

    /**
     * Function for recording player movement on the FlowPane
     * @throws IOException if the socket is closed
     */
    private void playersMove() throws IOException {
        String string = reader.readLine();
        Platform.runLater(() -> gameController.updatePlayersMoves(string));
    }

    /**
     * Function for setting the current session time in the client window
     * @throws IOException if the socket is closed
     */
    private void currentTimeSession() throws IOException {
        String time = reader.readLine();
        Platform.runLater(() -> gameController.updateSessionTime(time));
    }

    /**
     * Function for updating the state of a letter in a word
     * @throws IOException if the socket is closed
     */
    private void answerOnRequest() throws IOException {
        try {
            int value = Integer.parseInt(reader.readLine());
            int pos = Integer.parseInt(reader.readLine());
            Platform.runLater(() -> gameController.updateWord(value, pos));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Function starts the main part of the game
     * @throws IOException if the socket is closed
     */
    private void showWord() throws IOException {
        try {
            int word = Integer.parseInt(reader.readLine());
            Platform.runLater(() -> {
                gameController.showWord(word);
                gameController.firstLabel.setText("Время сессии:");
                gameController.firstLabel.setAlignment(Pos.CENTER);
            });
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Function for setting the remaining time to prepare for the game
     * @throws IOException if the socket is closed
     */
    private void preparationTime() throws IOException {
        String time = reader.readLine();
        if (time == null) {
            return;
        }
        Platform.runLater(() -> gameController.updatePreparationTime(time));
    }

    /**
     * Function setting the current list of players on the FlowPane
     * @throws IOException if the socket is closed
     */
    private void playersList() throws IOException {
        ObservableList<String> players = FXCollections.observableArrayList();
        try {
            int number = Integer.parseInt(reader.readLine());
            for (int i = 0; i < number; i++) {
                String player = reader.readLine();
                players.add(player);
            }
            Platform.runLater(() -> gameController.updatePlayers(players));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Function for sending a request to the server
     * @param symbol letter
     * @param pos position
     */
    void requestOnCheckCharacter(String symbol, int pos) {
        if (symbol.length() < 2) {
            try {
                String request = symbol + "\n" + pos + "\n";
                writer.write(request);
                writer.flush();
            } catch (IOException e) {
                Platform.runLater(gameController::stopGame);
            }
        }
    }
}
