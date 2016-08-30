package org.playerhook.games.api;

import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface Session extends MapSerializable {
    Game getGame();
    Board getBoard();
    List<Player> getPlayers();
    Optional<Player> getPlayerOnTurn();
    Optional<Player> getWinner();
    List<Move> getPlayedMoves();
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

    void play(TokenPlacement placement);

    @Override default Map<String, Object> toMap() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("game", getGame().toMap());
        builder.put("board", getBoard().toMap());
        builder.put("players", getPlayers().stream().map(Player::toMap).collect(Collectors.toList()));
        builder.put("scores", ImmutableMap.copyOf(getPlayers().stream().collect(Collectors.toMap(Player::getUsername, this::getScore))));
        builder.put("decks", ImmutableMap.copyOf(getPlayers().stream().collect(Collectors.toMap(Player::getUsername, (player1) -> getDeck(player1).toMap()))));
        builder.put("playedMoves", getPlayedMoves().stream().map(Move::toMap).collect(Collectors.toList()));
        builder.put("status", getStatus().name());

        getPlayerOnTurn().ifPresent(player -> builder.put("playerOnTurn", player.toMap()));
        getWinner().ifPresent(player -> builder.put("winner", player.toMap()));
        getURL().ifPresent(s -> builder.put("url", s.toExternalForm()));

        return builder.build();
    }
}
