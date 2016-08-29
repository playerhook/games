package org.playerhook.games.api;

public interface SessionUpdateType {

    enum Default implements SessionUpdateType {
        MOVE,
        PLAYER,
        STATUS
    }

}
