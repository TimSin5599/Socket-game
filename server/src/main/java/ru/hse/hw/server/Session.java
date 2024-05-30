package ru.hse.hw.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

    public Session(int playersNumber, int sessionPreparationTime, int sessionDurationLimit, int pauseTime, int successNotificationPeriod) {
        this.playersNumber = playersNumber;
        this.sessionPreparationTime = sessionPreparationTime;
        this.sessionDurationLimit = sessionDurationLimit;
        this.pauseTime = pauseTime;
        this.successNotificationPeriod = successNotificationPeriod;
        players = new ArrayList<>();
        connections = new ArrayList<>();
        time = System.currentTimeMillis();
    }

    @Override
    public void run() {

        // Добавить connection и после чтения имени добавлять в players
        // Как отправлять список игроков клиентам? (отправлять каждый раз после нового Connection, а есдли игрок вышел?)
    }

    void addPlayer(Connection connection) {
        connections.add(connection);
        timerPreparationTime(connection);
        try {
            BufferedReader reader = connection.getReader();
            String player = reader.readLine();
            players.add(player);
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
        for (Connection connection : connections) {
            try {
                BufferedWriter writer = connection.getWriter();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("playersList").append("\n");
                stringBuilder.append(players.size()).append("\n");
                System.out.println(players.size());
                for (String player : players) {
                    System.out.println(player);
                    stringBuilder.append(player).append("\n");
                }
                writer.write(stringBuilder.toString());
                writer.flush();
                System.out.println("Сервер отправил данные об игроках");
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
