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

public class Session implements Runnable{
    private final int playersNumber;
    private final int sessionPreparationTime;
    private final int sessionDurationLimit;
    private final int pauseTime;
    private final int successNotificationPeriod;
    private final List<Connection> connections;
    private final long time;
    private final String[] words;
    private String word;

    public Session(int playersNumber, int sessionPreparationTime, int sessionDurationLimit, int pauseTime, int successNotificationPeriod) {
        this.playersNumber = playersNumber;
        this.sessionPreparationTime = sessionPreparationTime;
        this.sessionDurationLimit = sessionDurationLimit;
        this.pauseTime = pauseTime;
        this.successNotificationPeriod = successNotificationPeriod;
        connections = new ArrayList<>();
        time = System.currentTimeMillis();
        words = WordsReader.readWords();
    }

    @Override
    public void run() {
        try (ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor()) {
            executorService.scheduleWithFixedDelay(this::updatePlayers, 0, 100, TimeUnit.MILLISECONDS);
            try {
                executorService.awaitTermination(sessionPreparationTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Random random = new Random();
        word = words[random.nextInt(words.length)];
        System.out.println(word);
        for (Connection connection : connections) {
            BufferedWriter writer = connection.getWriter();
            try {
                writer.write("word");
                writer.newLine();
                writer.write(String.valueOf(word.length()));
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        while (true) {
            for (Connection connection : connections) {
                checkCharacter(connection);
            }
        }
    }

    private void checkCharacter(Connection connection) {
        BufferedReader reader = connection.getReader();
        for (Connection conn : connections) {
            BufferedWriter writer = conn.getWriter();
            try {
                if (conn == connection) {
                    writer.write("gameCondition");
                    writer.newLine();
                    writer.write("1");
                    writer.newLine();
                    writer.flush();
                } else {
                    writer.write("gameCondition");
                    writer.newLine();
                    writer.write("0");
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        try {
//            reader.mark(sessionDurationLimit);
            String value = reader.readLine();
            int pos = Integer.parseInt(reader.readLine());
            System.out.println(value);
            if (value.length() == 1) {
                char c = value.charAt(0);
                BufferedWriter writer = connection.getWriter();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("answerOnRequest").append("\n");
                if (word.charAt(pos) == c) {
                    System.out.println("Буквы равны");
                    stringBuilder.append("1").append("\n");
                } else {
                    System.out.println("Буквы не совпали");
                    stringBuilder.append("0").append("\n");
                }
                stringBuilder.append(pos).append("\n");
                writer.write(stringBuilder.toString());
                writer.flush();
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    void addPlayer(Socket socket) throws IOException {
        Connection connection = new Connection(socket);
        connections.add(connection);
        try {
            BufferedReader reader = connection.getReader();
            String player = reader.readLine();
            connection.setName(player);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
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
                writer.newLine();
                writer.flush();

                writer.write("preparationTime");
                writer.newLine();
                writer.write(String.valueOf((sessionPreparationTime*1000L - System.currentTimeMillis() + time)));
                writer.newLine();
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
