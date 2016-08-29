package org.playerhook.games.api;

import rx.Observable;

import java.util.List;
import java.util.Optional;

public interface Session {
    Game getGame();
    Board getBoard();
    List<Player> getPlayers();
    Optional<Player> getPlayerOnTurn();
    Optional<Player> getWinner();
    List<Move> getPlayedMoves();
    Status getStatus();

    // Seat takeSeat(Player player);

    Deck getDeck(Player player);
    int getScore(Player player);

    default boolean isFinished() {
        return Status.FINISHED.equals(getStatus());
    }

    void play(TokenPlacement placement);

    Observable<SessionUpdate> asObservable();
}
