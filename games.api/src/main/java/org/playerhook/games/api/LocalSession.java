package org.playerhook.games.api;

public interface LocalSession extends Session {

    boolean hasEmptySeat();
    void join(Player newPlayer);
    boolean canStart();
    void start();

}
