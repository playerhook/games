package org.playerhook.games.util;

/**
 * Class used to represent the response from the web service in hooks.
 */
public class Acknowledgement {

    public static final Acknowledgement ACKNOWLEDGED = new Acknowledgement(true);
    public static final Acknowledgement NOT_ACKNOWLEDGED = new Acknowledgement(false);

    private boolean acknowledged;

    Acknowledgement(){}

    private Acknowledgement(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }
}
