package ru.hse.hw.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.io.*;
import java.net.Socket;

public class ClientConnection implements Runnable {
    private final String playersName;
    private final GameController gameController;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public ClientConnection(String addressServer, int port, String playersName, GameController gameController) throws IOException {
        this.playersName = playersName;
        this.gameController = gameController;
        socket = new Socket(addressServer, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

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
            case "gameCondition":
                gameCondition();
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
            case "winner":
                showWinner();
                break;
            case "stopGame":
                stopGame();
                break;
            default:
                break;
        }
    }

    private void stopGame() throws IOException {
        Platform.runLater(gameController::stopGame);
        throw new IOException();
    }

    private void updateLetterPlayer() throws IOException {
        try {
            String playerName = reader.readLine();
            int position = Integer.parseInt(reader.readLine());
            Platform.runLater(() -> gameController.updateLetterPlayer(playerName, position));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    private void initializeSession() throws IOException{
        try {
           int session_ID = Integer.parseInt(reader.readLine());
           Platform.runLater(() -> gameController.setSessionID(session_ID));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    private void pauseTime() {
        Platform.runLater(() -> gameController.firstLabel.setText("Игра скоро начнется!"));
    }

    private void showWinner() throws IOException {
        String nameWinner = reader.readLine();
        Platform.runLater(() -> gameController.showWinner(nameWinner));
    }

    private void playersMove() throws IOException {
        String string = reader.readLine();
        Platform.runLater(() -> gameController.updatePlayersMoves(string));
    }

    private void currentTimeSession() throws IOException {
        String time = reader.readLine();
        Platform.runLater(() -> gameController.updateSessionTime(time));
    }

    private void gameCondition() throws IOException {
        try {
            int value = Integer.parseInt(reader.readLine());
            if (value == 1) {
                Platform.runLater(gameController::openCharacters);
            } else {
                Platform.runLater(gameController::hideCharacters);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    private void answerOnRequest() throws IOException {
        try {
            int value = Integer.parseInt(reader.readLine());
            int pos = Integer.parseInt(reader.readLine());
            Platform.runLater(() -> gameController.updateWord(value, pos));
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

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

    private void preparationTime() throws IOException {
        String time = reader.readLine();
        if (time == null) {
            return;
        }
        Platform.runLater(() -> gameController.updatePreparationTime(time));
    }

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
