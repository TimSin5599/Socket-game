package ru.hse.hw.server;

import java.io.*;
import java.net.Socket;
import java.util.ListIterator;

public class Connection extends Thread {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String playerName;
    private int playerScore;
    private Session session;
    private int[] letterCondition;

    public Connection(Socket socket, Session session) throws IOException {
        this.socket = socket;
        this.session = session;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        playerScore = 0;
    }

    @Override
    public void run() {
        String word = session.getWord();
        letterCondition = new int[word.length()];
        while (true) {
            try {
                String value = reader.readLine();
                int pos = Integer.parseInt(reader.readLine());
                session.incrementOrder();

                if (value.length() == 1) { // Проверка, что пользователь ввел не более чем 1 букву
                    char c = value.charAt(0);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("answerOnRequest").append("\n");
                    int answerServer = -1;
                    if (word.charAt(pos) == c) {
                        letterCondition[pos] = 1;
                        stringBuilder.append("1").append("\n");
                        incrementScore();
                        answerServer = 1;
                    } else if (word.contains(value)) {
                        stringBuilder.append("0").append("\n");
                        answerServer = 0;
                    } else {
                        stringBuilder.append("-1").append("\n");
                    }

                    session.strokeRecord(this.getNamePlayer(), answerServer, pos, value);

                    stringBuilder.append(pos).append("\n");
                    writer.write(stringBuilder.toString());
                    writer.flush();

                    checkWin();
                }
            } catch (IOException | NumberFormatException e) {
                this.close();
                break;
            }
        }
    }

    private synchronized void checkWin() {
        String word = session.getWord();
        if (word.length() == playerScore) {
            session.stopGame();
        }
    }

    BufferedReader getReader() {
        return reader;
    }

    BufferedWriter getWriter() {
        return writer;
    }

    void setNamePlayer(String playerName) {
        this.playerName = playerName;
    }

    synchronized void incrementScore() {
        ++playerScore;
    }

    synchronized int getPlayerScore() {
        return playerScore;
    }

    String getNamePlayer() {
        return playerName;
    }

    int[] getLetterCondition() {
        return letterCondition;
    }

    void close() {
        try {
            if (!socket.isClosed()) {
                reader.close();
                writer.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
