package org.playerhook.games.tictactoe.hook.springboot;

public class Acknowledgement {

    public static final Acknowledgement ACKNOWLEDGED = new Acknowledgement(true);
    public static final Acknowledgement NOT_ACKNOWLEDGED = new Acknowledgement(false);

    private final boolean acknowledged;

    private Acknowledgement(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }
}
