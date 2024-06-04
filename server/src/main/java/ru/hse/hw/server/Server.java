package ru.hse.hw.server;

import ru.hse.hw.util.WordsReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final int playersNumber;
    private final int sessionPreparationTime;
    private final int sessionDurationLimit;
    private final int pauseTime;
    private final int successNotificationPeriod;

    public Server(int port, int playersNumber, int sessionPreparationTime, int sessionDurationLimit, int pauseTime, int successNotificationPeriod) throws IOException {
        serverSocket = new ServerSocket(port);
        this.playersNumber = playersNumber;
        this.sessionPreparationTime = sessionPreparationTime;
        this.sessionDurationLimit = sessionDurationLimit;
        this.pauseTime = pauseTime;
        this.successNotificationPeriod = successNotificationPeriod;
        System.out.println("Сервер запущен, порт сервера - " + port);
    }

    @Override
    public void run() {
        int countPlayers = 0;
        Session session = null;
        Thread thread = null;

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Соединение с клиентом установлено");
                System.out.println("Socket - " + socket);
                if (countPlayers < playersNumber && session != null && thread.isAlive()) {
                    countPlayers++;
                    session.addPlayer(socket);
                } else {
                    countPlayers = 0;
                    session = new Session(playersNumber, sessionPreparationTime, sessionDurationLimit, pauseTime, successNotificationPeriod);
                    session.addPlayer(socket);
                    thread = new Thread(session);
                    thread.start();
                    countPlayers++;
                }
//                Connection connection = new Connection(socket);
//                Thread thread = new Thread(connection);
//                thread.start();

            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.out.println("Сокет сервера закрыт");
        }
    }


    void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
