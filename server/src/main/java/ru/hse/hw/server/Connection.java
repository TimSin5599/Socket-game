package ru.hse.hw.server;

import java.io.*;
import java.net.Socket;

/**
 * The Connection class represents a connection to a client
 */
public class Connection extends Thread {
    /**
     * client socket
     */
    private final Socket socket;
    /**
     * The reader is used to read data from the socket
     */
    private final BufferedReader reader;
    /**
     * The writer is used to write data from the socket
     */
    private final BufferedWriter writer;
    /**
     * player name
     */
    private String playerName;
    /**
     * A personalised player counter that counts the number of letters guessed
     */
    private int playerScore;
    /**
     * Session in which the player participates
     */
    private Session session;
    /**
     * This field shows the current state of the letters in their places
     */
    private int[] letterCondition;
    /**
     * stroke order during the session
     */
    private int orderMove;

    /**
     * Connection builder
     * @param socket client socket
     * @param session Session in which the player participates
     * @throws IOException An IOException is thrown when a client socket is closed
     */
    public Connection(Socket socket, Session session) throws IOException {
        this.socket = socket;
        this.session = session;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        playerScore = 0;
        orderMove = 0;
    }

    /**
     * The {@code run()} method is used when running the game and is used to check the letters for position
     */
    @Override
    public void run() {
        String word = session.getWord();
        letterCondition = new int[word.length()];
        while (true) {
            try {
                String value = reader.readLine();
                int pos = Integer.parseInt(reader.readLine());
                incrementOrder();

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

                    strokeRecord(value, pos, answerServer);

                    stringBuilder.append(pos).append("\n");
                    writer.write(stringBuilder.toString());
                    writer.flush();

                    checkWin();
                }
            } catch (IOException | NumberFormatException  e) {
                this.close();
                break;
            }
        }
    }

    /**
     * The function is used to send user movements to the client window to record this information in the list of movements in the client window
     * @param answerServer server response
     * @param pos letter position
     * @param letter letter entered by the client
     */
    void strokeRecord(String letter, int pos, int answerServer) {
        String string = "playersMove\n" + orderMove + " " + letter + " " + pos + " " + answerServer + "\n";
        try {
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            this.close();
        }
    }

    /**
     * The function for checking player's winnings
     */
    private synchronized void checkWin() {
        String word = session.getWord();
        if (word.length() == playerScore) {
            session.stopGame();
        }
    }

    /**
     * @return reader
     */
    BufferedReader getReader() {
        return reader;
    }

    /**
     * @return writer
     */
    BufferedWriter getWriter() {
        return writer;
    }

    /**
     * This function is used to set player name
     * @param playerName player name
     */
    void setNamePlayer(String playerName) {
        this.playerName = playerName;
    }

    /**
     * The function is used to add a player's points by +1
     */
    void incrementScore() {
        ++playerScore;
    }

    /**
     * The function adds +1 to the order movement counter
     */
    void incrementOrder() {
        ++orderMove;
    }

    /**
     * @return player score
     */
    synchronized int getPlayerScore() {
        return playerScore;
    }

    /**
     * @return player name
     */
    String getNamePlayer() {
        return playerName;
    }

    /**
     * @return an array with the current state of the letters
     */
    int[] getLetterCondition() {
        return letterCondition;
    }

    /**
     * The function is used when the game ends and closes the socket, reader and writer
     */
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
