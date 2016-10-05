package org.playerhook.games.api;

import rx.Observable;

import java.net.URL;

public interface LocalSession extends Session {

    static LocalSession newSession(Game game, URL url) {
        return new DefaultLocalSession(game, url);
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


    static LocalSession load(Object session) {
        return DefaultLocalSession.load(session);
    }
}
