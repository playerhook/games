package org.playerhook.games.api;

import rx.Observable;

public interface LocalSession extends Session {

    void join(Player newPlayer);
    void start();
    Observable<SessionUpdate> asObservable();

    default boolean hasEmptySeat() {
        return getPlayers().size() < getGame().getMaxPlayers();
    }

    default boolean canStart() {
        return getPlayers().size() >= getGame().getMinPlayers() && getPlayers().size() <= getGame().getMaxPlayers();
    }

}
