package ru.hse.hw.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {
    private final ServerSocket serverSocket;
    private final int playersNumber;
    private final int sessionPreparationTime;
    private final int sessionDurationLimit;
    private final int pauseTime;
    private final int successNotificationPeriod;
    private final List<Session> listSessions;
    private int limitLetters;
    private String limitWord;

    public Server(int port, int playersNumber, int sessionPreparationTime, int sessionDurationLimit, int pauseTime, int successNotificationPeriod) throws IOException {
        serverSocket = new ServerSocket(port);
        this.playersNumber = playersNumber;
        this.sessionPreparationTime = sessionPreparationTime;
        this.sessionDurationLimit = sessionDurationLimit;
        this.pauseTime = pauseTime;
        this.successNotificationPeriod = successNotificationPeriod;
        this.listSessions = new ArrayList<>();
        limitLetters = 0;
        limitWord = "";
        System.out.println("Сервер запущен, порт сервера - " + port);
    }

    @Override
    public void run() {
        Session session = null;
        int session_ID = 0;
        long timer = 0;

        try (ScheduledExecutorService serviceUpdateSessions = Executors.newSingleThreadScheduledExecutor()) {
            serviceUpdateSessions.scheduleWithFixedDelay(() -> {
                ListIterator<Session> listIterator = listSessions.listIterator();
                while (listIterator.hasNext()) {
                    Session game = listIterator.next();
                    if (!game.isAlive()) {
                        listIterator.remove();
                    }
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Соединение с клиентом установлено");
                if (session != null && !session.getStatus() && ((System.currentTimeMillis() - timer) < sessionPreparationTime*1000L || sessionPreparationTime == 0)) {
                    session.addPlayer(socket);
                } else {
                    ++session_ID;
                    session = new Session(session_ID, playersNumber, sessionPreparationTime, sessionDurationLimit,
                            pauseTime, successNotificationPeriod, limitLetters, limitWord);
                    listSessions.add(session);
                    session.addPlayer(socket);
                    session.start();
                    timer = System.currentTimeMillis();
                }
            }
        } catch (IOException e) {
            System.out.println("Сокет сервера закрыт");
        }
    }

    void stopServer() {
        try {
            for (Session session : listSessions) {
                List<Connection> listConnections = session.getConnections();
                for (Connection connection : listConnections) {
                    BufferedWriter writer = connection.getWriter();
                    try {
                        writer.write("stopGame\n");
                        writer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    connection.close();
                }
            }
        }
        finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void setLimit(String value) {
        try {
            limitLetters = Integer.parseInt(value);
            limitWord = "";
        } catch (NumberFormatException e) {
            limitWord = value;
            limitLetters = 0;
        }
    }
}
