package ru.hse.hw.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.Socket;

public class ClientConnection implements Runnable {
    private final String addressServer;
    private final int port;
    private final String playersName;
    private final GameController gameController;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public ClientConnection(String addressServer, int port, String playersName, GameController gameController) throws IOException {
        this.addressServer = addressServer;
        this.port = port;
        this.playersName = playersName;
        this.gameController = gameController;
        socket = new Socket(addressServer, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            System.out.println("Клиент установил связь с сервером");
            writer.write(playersName);
            writer.newLine();
            writer.flush();

            while (Platform.isImplicitExit()) {
                String category = reader.readLine();
                System.out.println(category);
                dataCategory(category);
            }
        } catch (IOException e) {
            System.out.println("Сокет клиента закрыт");
//            e.printStackTrace(System.err);
        }
    }

    private void dataCategory(String category) throws IOException {
        switch (category) {
            case "playersList":
                playersList();
                break;
            case "preparationTime":
                preparationTime();
                break;
            case "word":
                showWord();
                break;
            case "answerOnRequest":
                answerOnRequest();
                break;
            case "gameCondition":
                gameCondition();
            default:
                break;
        }
    }

    private void gameCondition() {
        try {
            int value = Integer.parseInt(reader.readLine());
            if (value == 1) {
                Platform.runLater(gameController::openCharacters);
            } else {
                Platform.runLater(gameController::hideCharacters);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void answerOnRequest() {
        try {
            int value = Integer.parseInt(reader.readLine());
            System.out.println(value);
            int pos = Integer.parseInt(reader.readLine());
            System.out.println(pos);
            Platform.runLater(() -> gameController.updateWord(value, pos));
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    private void showWord() {
        try {
            int word = Integer.parseInt(reader.readLine());
            System.out.println(word);
            Platform.runLater(() -> gameController.showWord(word));
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    private void preparationTime() throws IOException {
        String time = reader.readLine();
        if (time == null) {
            return;
        }
        Platform.runLater(() -> gameController.updatePreparationTime(time));
    }

    private void playersList() throws IOException {
        ObservableList<String> players = FXCollections.observableArrayList();
        int number = Integer.parseInt(reader.readLine());
//        System.out.println(number);
        for (int i = 0; i < number; i++) {
            String player = reader.readLine();
            players.add(player);
//            System.out.println(player);
        }
        Platform.runLater(() -> gameController.updatePlayers(players));
    }

    void requestOnCheckCharacter(String symbol, int pos) {
        System.out.println(symbol);
        if (symbol.length() < 2) {
            try {
                System.out.println(symbol);
                writer.write(symbol);
                writer.newLine();
                writer.write(String.valueOf(pos));
                writer.newLine();
                writer.flush();
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    void close() {
        try {
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
