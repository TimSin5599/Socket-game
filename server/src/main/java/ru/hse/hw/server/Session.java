package ru.hse.hw.server;

import ru.hse.hw.util.WordsReader;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Session implements Runnable{
    private final int playersNumber;
    private final int sessionPreparationTime;
    private final int sessionDurationLimit;
    private final int pauseTime;
    private final int successNotificationPeriod;
    private volatile List<String> players;
    private List<Connection> connections;
    private final long time;
    private final String[] words;

    public Session(int playersNumber, int sessionPreparationTime, int sessionDurationLimit, int pauseTime, int successNotificationPeriod) {
        this.playersNumber = playersNumber;
        this.sessionPreparationTime = sessionPreparationTime;
        this.sessionDurationLimit = sessionDurationLimit;
        this.pauseTime = pauseTime;
        this.successNotificationPeriod = successNotificationPeriod;
        players = new ArrayList<>();
        connections = new ArrayList<>();
        time = System.currentTimeMillis();
        words = WordsReader.readWords();
    }

    @Override
    public void run() {
        Random random = new Random();
        String word = words[random.nextInt(words.length)];
        // Добавить connection и после чтения имени добавлять в players
        // Как отправлять список игроков клиентам? (отправлять каждый раз после нового Connection, а есдли игрок вышел?)
    }

    void addPlayer(Connection connection) {
        connections.add(connection);
        timerPreparationTime(connection);
        try {
            BufferedReader reader = connection.getReader();
            String player = reader.readLine();
            connection.setName(player);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        updatePlayers();
    }

    private void timerPreparationTime(Connection connection) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            if ((System.currentTimeMillis() - time) / 1000 < sessionPreparationTime) {
                try {
                    /*ListIterator<Connection> listIterator = connections.listIterator();
                    int count = 0;
                    while (listIterator.hasNext()) {
                        Connection conn = listIterator.next();
                        if (!conn.isConnected()) {
                            listIterator.remove();
                            ++count;
                        }
                    }
                    if (count > 0) {
                        System.out.println(count);
                        updatePlayers();
                    }*/
                    updatePlayers();

                    connection.preparationTime(String.valueOf(sessionPreparationTime*1000L - System.currentTimeMillis() + time));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (executorService) {
                    connection.preparationTime("Время истекло");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    executorService.shutdown();
                }
            }
        }, 0, 4, TimeUnit.MILLISECONDS);

    }

    private void updatePlayers() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("playersList").append("\n");
        stringBuilder.append(connections.size()).append("\n");

        System.out.println(connections.size());

        for (Connection conn : connections) {
            System.out.println(conn.getName());
            stringBuilder.append(conn.getName()).append("\n");
        }

        ListIterator<Connection> listIterator = connections.listIterator();
        while (listIterator.hasNext()) {
            Connection connection = listIterator.next();
            try {
                BufferedWriter writer = connection.getWriter();
                writer.write(stringBuilder.toString());
                writer.flush();
                System.out.println("Сервер отправил данные об игроках");
            } catch (IOException e) {
                listIterator.remove();
                try {
                    connection.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace(System.out);
            }
        }
    }
}
