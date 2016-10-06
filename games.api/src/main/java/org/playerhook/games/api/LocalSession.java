package org.playerhook.games.api;

import rx.Observable;

import java.net.URL;

public interface LocalSession extends Session {

    /**
     * Creates new session. If the session with given URL already exists, returns the existing one if not yet finished
     * otherwise creates new one.
     *
     * @param game game for this session
     * @param url url uniquely identifying the session
     * @return existing or new session
     * @throws IllegalArgumentException if the session already exists with different game
     */
    static LocalSession findOrCreate(Game game, URL url) {
        return DefaultLocalSession.get(game, url);
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

    /**
     * Loads session bases on given payload.
     *
     * If the payload contains URL and session with the given URL already exists and it has not been finished yet
     * the existing session is returned and updated with the payload.
     *
     * @param payload payload representing the session
     * @return new session for the payload or existing session updated from the payload
     */
    static LocalSession load(Object payload) {
        return DefaultLocalSession.load(payload);
    }

    /**
     * Finds the existing session by given URL.
     *
     * If the session is already finished or does not exist yet returns null.
     *
     * @param url url uniquely identifying the session
     * @return the existing session if exists or null otherwise
     */
    static LocalSession find(URL url) {
        return DefaultLocalSession.find(url);
    }
}
