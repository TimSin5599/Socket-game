package ru.hse.hw.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{
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

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Соединение с клиентом установлено");
                System.out.println("Socket - " + socket);
                if (countPlayers < playersNumber && session != null) {
                    countPlayers++;
                    Connection connection = new Connection(socket, session);
                    session.addPlayer(connection);
                } else {
                    countPlayers = 0;
                    session = new Session(playersNumber, sessionPreparationTime, sessionDurationLimit, pauseTime, successNotificationPeriod);
                    Connection connection = new Connection(socket, session);
                    session.addPlayer(connection);
                    countPlayers++;
                }
//                Connection connection = new Connection(socket);
//                Thread thread = new Thread(connection);
//                thread.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
