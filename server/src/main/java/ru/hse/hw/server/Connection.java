package ru.hse.hw.server;

import java.io.*;
import java.net.Socket;

public class Connection {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final Session session;
    private String playerName;

    public BufferedReader getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public Connection(Socket socket, Session session) throws IOException {
        this.socket = socket;
        this.session = session;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void preparationTime(String string) throws IOException {
        writer.write("preparationTime");
        writer.newLine();
        writer.flush();

        writer.write(String.valueOf(string));
        writer.newLine();
        writer.flush();
    }

    void setName(String playerName) {
        this.playerName = playerName;
    }

    String getName() {
        return playerName;
    }

    void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }
    /*@Override
    public void run() {
        try (socket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            System.out.println("Соединение закрыто");
        }
    }*/
}
