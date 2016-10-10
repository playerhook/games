package org.playerhook.games.api;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.util.MapSerializable;
import org.playerhook.games.util.Versioned;

import java.net.URL;
import java.util.Optional;

public interface Session extends MapSerializable, Versioned {
    Game getGame();
    Board getBoard();
    ImmutableList<Player> getPlayers();
    Optional<Player> getPlayerOnTurn();
    ImmutableList<Move> getMoves();
    Status getStatus();
    Optional<URL> getURL();

    /**
     * @param player the player which deck should be returned
     * @return the deck for given player as visible for player on turn.
     */
    Deck getDeck(Player player);
    int getScore(Player player);

    default boolean isFinished() {
        return Status.FINISHED.equals(getStatus());
    }

    static Session load(Object session) {
        return DefaultSession.load(session);
    }
}
