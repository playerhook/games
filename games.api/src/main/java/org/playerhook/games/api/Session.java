package org.playerhook.games.api;

import java.util.List;
import java.util.Optional;

public interface Session {
    Game getGame();
    Board getBoard();
    List<Player> getPlayers();
    Optional<Player> getOnTurn();
    Optional<Player> getWinner();
    List<Move> getPlayedMoves();
    Status getStatus();

    // Seat takeSeat(Player player);

    Deck getDeck(Player player);
    int getScore(Player player);
    void play(TokenPlacement placement);
}
