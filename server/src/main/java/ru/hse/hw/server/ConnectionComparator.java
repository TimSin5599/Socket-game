package ru.hse.hw.server;

import java.util.Comparator;

public class ConnectionComparator implements Comparator<Connection> {
    @Override
    public int compare(Connection o1, Connection o2) {
        return o2.getPlayerScore() - o1.getPlayerScore();
    }
}
