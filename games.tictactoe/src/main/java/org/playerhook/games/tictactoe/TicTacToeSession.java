package org.playerhook.games.tictactoe;

import com.google.common.collect.ImmutableList;

import org.playerhook.games.api.*;

import java.net.URL;

public class TicTacToeSession extends AbstractSession {

    private static final String GAME_TITLE = "Tic Tac Toe";
    private static final Game GAME = Game.of(GAME_TITLE, 2);

    private static final ImmutableList<ImmutableList<Direction>> COUNTING_DIRECTION = ImmutableList.of(
      ImmutableList.of(Direction.DOWN, Direction.UP),
      ImmutableList.of(Direction.RIGHT, Direction.LEFT),
      ImmutableList.of(Direction.UPPER_LEFT, Direction.LOWER_RIGHT),
      ImmutableList.of(Direction.UPPER_RIGHT, Direction.LOWER_LEFT)
    );

    private final int toWin;

    public TicTacToeSession(int size, int toWin, URL url) {
        super(GAME, getSquare(size, toWin), url);
        this.toWin = toWin;
    }

    public TicTacToeSession(int size, int toWin) {
        super(GAME, getSquare(size, toWin), null);
        this.toWin = toWin;
    }

    private static Board getSquare(int size, int toWin) {
        if (toWin > size) {
            throw new IllegalArgumentException("The number of crosses or circles in row to win (" + toWin + ") cannot be less than the grid size (" + size + ")!");
        }
        return Board.square(size);
    }

    @Override
    public Deck getDeck(Player player) {
        if (!getPlayers().contains(player)) {
            throw new IllegalArgumentException("Player " + player + " does not play this game!");
        }
        if (getPlayers().indexOf(player) == 0) {
            return Deck.of(TicTacToeTokens.CROSS);
        }
        return Deck.of(TicTacToeTokens.CIRCLE);
    }

    @Override
    public int getScore(Player player) {
        if (!Status.FINISHED.equals(getStatus())) {
            return 0;
        }
        if (getWinner().map(player::equals).orElse(false)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void play(TokenPlacement placement) {
        if (doGenericChecks(placement)) {
            return;
        }

        setBoard(getBoard().place(placement));
        move(Move.of(placement));

        for (ImmutableList<Direction> directions : COUNTING_DIRECTION) {
            int count = countAround(placement, directions);
            if (count >= toWin) {
                setWinner(placement.getPlayer());
                changeState(Status.FINISHED);
                return;
            }
        }

        for (Player player : getPlayers()) {
            if (!player.equals(placement.getPlayer())) {
                setActivePlayer(player);
                if (getDeck(player).getPlayableTokens().size() == 0) {
                    changeState(Status.FINISHED);
                    return;
                }
            }
        }

        if (getBoard().isCompletelyFilled()) {
            changeState(Status.FINISHED);
        }

    }

    private int countAround(TokenPlacement placement, ImmutableList<Direction> directions) {
        Token expected = placement.getToken();

        int count = 1;

        for  (Direction direction : directions) {
            Position position = placement.getDestination().at(direction);

            while (getBoard().getTokenPlacement(position).map(other -> expected.equals(other.getToken())).orElse(false)) {
                count++;
                position = position.at(direction);
            }
        }

        return count;
    }
}
