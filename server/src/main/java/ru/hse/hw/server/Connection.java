package ru.hse.hw.server;

import java.io.*;
import java.net.Socket;

public class Connection {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String playerName;

    public BufferedReader getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
}
