package ru.hse.hw.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
        try (socket; reader; writer) {
            System.out.println("Клиент установил связь с сервером");
            writer.write(playersName);
            writer.newLine();
            writer.flush();

            while (Platform.isImplicitExit()) {
                try {
                    String category = reader.readLine();
                    dataCategory(category);
                } catch (NumberFormatException e) {
                    e.printStackTrace(System.err);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
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
            default:
                break;
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
        System.out.println(number);
        for (int i = 0; i < number; i++) {
            String player = reader.readLine();
            players.add(player);
            System.out.println(player);
        }
        Platform.runLater(() -> gameController.updatePlayers(players));
    }
}
