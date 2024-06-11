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

/**
 * The Server class is used as a server which accepts players
 */
public class Server extends Thread {
    /**
     * Server socket
     */
    private final ServerSocket serverSocket;
    /**
     * Number of players in one session
     */
    private final int playersNumber;
    /**
     * The time to be spent preparing for the session
     */
    private final int sessionPreparationTime;
    /**
     * Time during which the session will run
     */
    private final int sessionDurationLimit;
    /**
     *  The time which will elapse between the preparation for the session and the main part of the game
     */
    private final int pauseTime;
    /**
     * The time used to notify to players
     */
    private final int successNotificationPeriod;
    /**
     * List of sessions
     */
    private volatile List<Session> listSessions;
    /**
     * This field is used to limit the quantity of letters in word
     */
    private int limitLetters;
    /**
     * This field is used to set the riddle word
     */
    private String limitWord;

    /**
     * Server builder
     * @param port the port which the server will use
     * @param playersNumber Number of players in one session
     * @param sessionPreparationTime The time to be spent preparing for the session
     * @param sessionDurationLimit Time during which the session will run
     * @param pauseTime The time which will elapse between the preparation for the session and the main part of the game
     * @param successNotificationPeriod The time used to notify to players
     * @throws IOException if an I/ O error occurs when opening the socket
     */
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

    /**
     * The {@code run()} function is used to accept clients into the game
     * The {@code serviceUpdateSessions} is used to leave sessions that are running
     */
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
        } finally {
            stopServer();
        }
    }

    /**
     * The function is used to close all connections and the server socket
     */
    void stopServer() {
        try {
            ListIterator<Session> listSessionsIterator = listSessions.listIterator();
            while (listSessionsIterator.hasNext()) {
                Session game = listSessionsIterator.next();
                ListIterator<Connection> connectionsIterator = game.getConnections().listIterator();
                while (connectionsIterator.hasNext()) {
                    Connection connection = connectionsIterator.next();
                    BufferedWriter writer = connection.getWriter();
                    try {
                        writer.write("stopGame\n");
                        writer.flush();
                    } catch (IOException ignore) {
                    }
                    if (connection.isAlive()) {
                        connection.close();
                    }
                }
                if (game.isAlive()) {
                    game.interrupt();
                }
            }
            /*for (Session session : listSessions) {
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
            }*/
        }
        finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The function sets a value to limit the number of letters or a riddle word
     * @param value
     */
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
