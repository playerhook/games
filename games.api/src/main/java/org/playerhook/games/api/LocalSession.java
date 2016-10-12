package org.playerhook.games.api;

import java.net.URL;
import java.util.Optional;

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
    static LocalSession create(Game game, URL url) {
        return DefaultLocalSession.newSession(game, url);
    }

    LocalSession join(Player newPlayer);
    LocalSession play(TokenPlacement placement);

    LocalSession signWith(String privateKey);

    Optional<String> getKey(Player player);

    default TokenPlacement sign(TokenPlacement placement) {
        Optional<String> key = getKey(placement.getPlayer());
        if (key.isPresent()) {
            return placement.sign(key.get());
        }
        return placement;
    }

    LocalSession start();
    LocalSession suspend();
    LocalSession resume();

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

    default ObservableLocalSession asObservableSession() {
        if (this instanceof DefaultObservableLocalSession) {
            return (ObservableLocalSession) this;
        }
        return new DefaultObservableLocalSession(this);
    }

}
