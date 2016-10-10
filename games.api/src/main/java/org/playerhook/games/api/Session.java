package org.playerhook.games.api;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.util.MapSerializable;

import java.net.URL;
import java.util.Optional;

public interface Session extends MapSerializable {
    Game getGame();
    Board getBoard();
    ImmutableList<Player> getPlayers();
    Optional<Player> getPlayerOnTurn();
    ImmutableList<Move> getMoves();
    Status getStatus();
    Optional<URL> getURL();
    Long getRound();

    /**
     * @param player the player which deck should be returned
     * @return the deck for given player as visible for player on turn.
     */
    Deck getDeck(Player player);
    int getScore(Player player);

    default TokenPlacement newPlacement(Token token, Player player, Position destination) {
        return newPlacement(token, player, null, destination);
    }

    TokenPlacement newPlacement(Token token, Player player, Position source, Position destination);

    default boolean isFinished() {
        return Status.FINISHED.equals(getStatus());
    }

    default Optional<Move> getLastMove() {
        if (getMoves().size() > 0) {
            return Optional.of(getMoves().get(0));
        }
        return Optional.empty();
    }

    static Session load(Object session) {
        return DefaultSession.load(session);
    }
}
