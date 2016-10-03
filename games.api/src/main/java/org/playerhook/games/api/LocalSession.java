package org.playerhook.games.api;

import rx.Observable;

import java.net.URL;

public interface LocalSession extends Session {

    static LocalSession newSession(Game game, Board board, URL url) {
        return new DefaultLocalSession(game, board, url);
    }

    void join(Player newPlayer);
    void start();
    Observable<SessionUpdate> asObservable();

    default boolean hasEmptySeat() {
        return getPlayers().size() < getGame().getRules().getMaxPlayers();
    }

    default boolean canStart() {
        return getPlayers().size() >= getGame().getRules().getMinPlayers() && getPlayers().size() <= getGame().getRules().getMaxPlayers();
    }

}
