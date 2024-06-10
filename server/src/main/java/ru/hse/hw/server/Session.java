package ru.hse.hw.server;

import ru.hse.hw.util.WordsReader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Session extends Thread {
    /**
     * the number of players
     */
    private final int playersNumber;
    /**
     * preparation time for the session
     */
    private final int sessionPreparationTime;
    /**
     * session limitation
     */
    private final int sessionDurationLimit;
    /**
     * the time used between the preparation session and the start of the game
     */
    private final int pauseTime;
    /**
     * the time used to notify to players
     */
    private final int successNotificationPeriod;
    /**
     * connection list
     */
    private final List<Connection> connections;
    /**
     * current time for different goals
     */
    private long time;
    /**
     * word list
     */
    private String[] words;
    /**
     * riddle word
     */
    private String word;
    /**
     * stroke order during the session
     */
    private volatile int orderMove;
    /**
     * the limit of letters
     */
    private final int limitLetters;
    /**
     * session_ID
     */
    private final int session_ID;
    /**
     * the service that is used during the game
     */
    private final ScheduledExecutorService serviceDurationSession;
    /**
     * game status
     */
    private volatile boolean status;

    /**
     * Session builder
     * @param session_ID session_ID
     * @param playersNumber the number of players
     * @param sessionPreparationTime preparation time for the session
     * @param sessionDurationLimit session limitation
     * @param pauseTime the time used between the preparation session and the start of the game
     * @param successNotificationPeriod the time used to notify to players
     * @param limitLetters the limit of letters
     * @param limitWord riddle word from the server
     */
    public Session(int session_ID, int playersNumber, int sessionPreparationTime, int sessionDurationLimit, int pauseTime, int successNotificationPeriod, int limitLetters, String limitWord) {
        this.session_ID = session_ID;
        this.playersNumber = playersNumber;
        this.sessionPreparationTime = sessionPreparationTime;
        this.sessionDurationLimit = sessionDurationLimit;
        this.pauseTime = pauseTime;
        this.successNotificationPeriod = successNotificationPeriod;
        this.status = false;
        connections = new ArrayList<>();
        time = System.currentTimeMillis();
        this.limitLetters = limitLetters;
        serviceDurationSession = Executors.newSingleThreadScheduledExecutor();

        if (!limitWord.isEmpty()) {
            word = limitWord;
        } else {
            words = WordsReader.readWords();
        }
        orderMove = 0;
    }

    /**
     * The main method in the Session class that implements the game logic
     * The {@code serviceDurationSession} is used to send the current game time
     */
    @Override
    public void run() {
        try (ScheduledExecutorService updatePlayersService = Executors.newSingleThreadScheduledExecutor();
             ScheduledExecutorService serviceNotification = Executors.newSingleThreadScheduledExecutor()) {
            updatePlayersService.scheduleWithFixedDelay(this::updatePlayers, 0, 100, TimeUnit.MILLISECONDS);

            preparationSession();

            if (connections.isEmpty()) {
                Thread.currentThread().interrupt();
                return;
            }

            pauseSession();

            status = true;

            // In this step, the session defines the word to be guessed
            Random random = new Random();
            if (limitLetters != 0) {
                word = words[random.nextInt(words.length)];
                while (word.length() != limitLetters) {
                    word = words[random.nextInt(words.length)];
                }
            } else if (word == null) {
                word = words[random.nextInt(words.length)];
            }
            System.out.println(word);

            ListIterator<Connection> listIterator = connections.listIterator();
            while (listIterator.hasNext()) { // sends clients the length of the riddle word
                Connection connection = listIterator.next();
                String string = "word\n" + word.length() + "\n";
                write(connection, string, listIterator);
                connection.start();
            }
            time = System.currentTimeMillis();


            serviceDurationSession.scheduleWithFixedDelay(() -> {
                ListIterator<Connection> iterator = connections.listIterator();
                while (iterator.hasNext()) {
                    Connection connection = iterator.next();
                    String string = "currentTimeSession\n" + ((System.currentTimeMillis() - time) / 1000) + "\n";
                    write(connection, string, iterator);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            // Service for sending notifications to users
            if (successNotificationPeriod > 0) {
                serviceNotification.scheduleWithFixedDelay(this::updateLetterPlayer, 0, successNotificationPeriod, TimeUnit.SECONDS);
            }

            // Wait for users to guess the word or time runs out
            if (sessionDurationLimit > 0) {
                serviceDurationSession.awaitTermination(sessionDurationLimit, TimeUnit.SECONDS);
            } else {
                serviceDurationSession.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            }
            serviceNotification.shutdown();
            updateLetterPlayer();

            if (!serviceDurationSession.isShutdown()) {
                serviceDurationSession.shutdown();
            }

            printWinner();

        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        } finally {
            for (Connection connection : connections) {
                connection.interrupt();
                connection.close();
            }
            System.out.println("Все клиенты в сессии № " + session_ID + " закрыты");
        }
    }

    /**
     * The function is used to update the list of players in all sessions
     */
    private void updatePlayers() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("playersList").append("\n");
        stringBuilder.append(connections.size()).append("\n");

        for (Connection conn : connections) {
            stringBuilder.append(conn.getNamePlayer()).append("\n");
        }

        ListIterator<Connection> listIterator = connections.listIterator();
        while (listIterator.hasNext()) {
            Connection connection = listIterator.next();
            write(connection, stringBuilder.toString(), listIterator);
        }
    }

    /**
     * The function is used between the preparation time and the main part of the game to create a pause
     * The {@code while} is used to send players information about the start of a pause
     * The {@code executorServicePauseTime} parameter is used to send the remaining time before the game starts with a period of 100 milliseconds
     */
    private void pauseSession() {
        ListIterator<Connection> list = connections.listIterator();
        while (list.hasNext()) {
            Connection connection = list.next();
            write(connection, "pauseTime\n", list);
        }

        time = System.currentTimeMillis();
        try (ScheduledExecutorService executorServicePauseTime = Executors.newSingleThreadScheduledExecutor()) {
            executorServicePauseTime.scheduleWithFixedDelay(() -> {
                ListIterator<Connection> listIterator = connections.listIterator();
                while (listIterator.hasNext()) {
                    Connection connection = listIterator.next();
                    String string = "preparationTime\n" + (pauseTime - (System.currentTimeMillis() - time) / 1000) + "\n";
                    write(connection, string, listIterator);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
            executorServicePauseTime.awaitTermination(pauseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * The function is used during the preparation period for the session
     * The {@code executorServicePreparationSession} parameter is used to send players the remaining time before the game starts
     */
    private void preparationSession() {
        try (ScheduledExecutorService executorServicePreparationSession = Executors.newSingleThreadScheduledExecutor()) {
            if (sessionPreparationTime > 0) {
                executorServicePreparationSession.scheduleWithFixedDelay(() -> {

                    ListIterator<Connection> listIterator = connections.listIterator();
                    while (listIterator.hasNext()) {
                        Connection connection = listIterator.next();
                        write(connection, "preparationTime\n" + (sessionPreparationTime - (System.currentTimeMillis() - time) / 1000) + "\n", listIterator);
                    }

                    if (connections.size() >= playersNumber) {
                        executorServicePreparationSession.shutdown();
                    }
                }, 0, 100, TimeUnit.MILLISECONDS);
                executorServicePreparationSession.awaitTermination(sessionPreparationTime, TimeUnit.SECONDS);
            } else {
                ListIterator<Connection> listIterator = connections.listIterator();
                while (listIterator.hasNext()) {
                    Connection connection = listIterator.next();
                    write(connection, """
                            preparationTime
                            Ожидание игроков
                            """, listIterator);
                }

                executorServicePreparationSession.scheduleWithFixedDelay(() -> {
                    if (connections.size() >= playersNumber) {
                        executorServicePreparationSession.shutdown();
                    }
                }, 0, 100, TimeUnit.MILLISECONDS);

                executorServicePreparationSession.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * The function is used to send the user's movements to the players to record this information in movement list in the client window
     * @param playerName player name
     * @param answerServer server response
     * @param pos letter position
     * @param letter letter entered by the client
     */
    synchronized void strokeRecord(String playerName, int answerServer, int pos, String letter) {
        ListIterator<Connection> listIterator = connections.listIterator();
        while (listIterator.hasNext()) {
            Connection connection = listIterator.next();
            String string = "playersMove\n" + orderMove + " " + playerName + " " +
                    pos + " " + letter + " " + answerServer + "\n";
            write(connection, string, listIterator);
        }
    }

    /**
     * The function is used to send the winning player in all sessions and to stop the game
     */
    synchronized void printWinner() {
        String nameWinner = "";
        for (Connection connection : connections) {
            if (connection.getPlayerScore() == word.length()) {
                nameWinner = connection.getNamePlayer();
                break;
            }
        }

        ListIterator<Connection> listIterator = connections.listIterator();
        while (listIterator.hasNext()) {
            Connection connection = listIterator.next();
            connection.interrupt();
            write(connection, "winner\n" + nameWinner + "\n", listIterator);
        }
    }

    /**
     * This function is used to add a client to each user's player list
     * @param socket client socket
     * @throws IOException IOException is thrown when the socket is closed
     */
    void addPlayer(Socket socket) throws IOException {
        Connection connection = new Connection(socket, this);
        connections.add(connection);

        try {
            BufferedReader reader = connection.getReader();
            String player = reader.readLine();
            connection.setNamePlayer(player);

            BufferedWriter writer = connection.getWriter();
            String string = "session_ID\n" + session_ID + "\n";
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            connection.close();
            connections.remove(connection);
            e.printStackTrace(System.err);
        }
    }

    /**
     * The function is used to send the guessed letters to the players
     */
    synchronized void updateLetterPlayer() {
        for (Connection connection : connections) {
            int[] letterCondition = connection.getLetterCondition();

            for (int i = 0; i < letterCondition.length; i++) {
                if (letterCondition[i] == 1) {
                    ListIterator<Connection> iterator = connections.listIterator();
                    String string = "updateLetterPlayer\n" + connection.getNamePlayer() + "\n" + i + "\n";

                    while (iterator.hasNext()) {
                        Connection conn = iterator.next();
                        write(conn, string, iterator);
                    }
                }
            }
        }
    }

    /**
     * The function writes a specific letter to a concrete socket and, if socket has been closed,
     * this connection is removed from the list of connections
     * @param connection connection
     * @param letter letter to be written to the connection
     * @param iterator connection iterator in the connection list
     */
    private void write(Connection connection, String letter, ListIterator<Connection> iterator) {
        BufferedWriter writer = connection.getWriter();
        try {
            writer.write(letter);
            writer.flush();
        } catch (IOException e) {
            iterator.remove();
            connection.close();
        }
    }

    /**
     * The function returns the list of connection
     * @return the list of connections
     */
    List<Connection> getConnections() {
        return connections;
    }

    /**
     * The function stops the game due to tje determination of the winner
     */
    synchronized void stopGame() {
        serviceDurationSession.shutdown();
    }

    /**
     * The function adds +1 to the order movement counter
     */
    synchronized void incrementOrder() {
        ++orderMove;
    }

    /**
     * @return the current game state
     */
    boolean getStatus() {
        return status;
    }

    /**
     * @return the guessed word
     */
    public String getWord() {
        return word;
    }
}
