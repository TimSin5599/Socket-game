package ru.hse.hw.client;

/**
 * The Move class is used for creating elements TableView
 */
public class Move {
    private final int orderMove;
    private final String letter;
    private final int place;
    private final int serverResponse;

    /**
     * Move builder
     * @param orderMove order move
     * @param letter letter
     * @param place place
     * @param serverResponse server responsible
     */
    public Move(int orderMove, String letter, int place, int serverResponse) {
        this.orderMove = orderMove;
        this.letter = letter;
        this.place = place;
        this.serverResponse = serverResponse;
    }

    /**
     * @return order move
     */
    public int getOrderMove() {
        return orderMove;
    }

    /**
     * @return letter
     */
    public String getLetter() {
        return letter;
    }

    /**
     * @return place
     */
    public int getPlace() {
        return place;
    }

    /**
     * @return server responsible
     */
    public int getServerResponse() {
        return serverResponse;
    }
}
