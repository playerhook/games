package org.playerhook.games.tictactoe;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.api.*;

import java.net.URL;

import static org.playerhook.games.api.LocalSession.newSession;

public class TicTacToeRules implements Rules {

    private static final String GAME_NAME = "Tic Tac Toe";
    private static final String GAME_DESCRIPTION = "Create line of tokens to win";

    public static LocalSession matchThree(URL gameUrl, URL sessionUrl) {
        return newSession(Game.of(GAME_NAME, GAME_DESCRIPTION, gameUrl, new Three()), sessionUrl);
    }

    public static LocalSession matchFour(URL gameUrl, URL sessionUrl) {
        return newSession(Game.of(GAME_NAME, GAME_DESCRIPTION, gameUrl, new Four()), sessionUrl);
    }

    public static LocalSession matchFive(URL gameUrl, URL sessionUrl) {
        return newSession(Game.of(GAME_NAME, GAME_DESCRIPTION, gameUrl, new Five()), sessionUrl);
    }

    public static LocalSession matchSix(URL gameUrl, URL sessionUrl) {
        return newSession(Game.of(GAME_NAME, GAME_DESCRIPTION, gameUrl, new Six()), sessionUrl);
    }

    public static class Three extends TicTacToeRules {
        public Three() {
            super(3);
        }
    }

    public static class Four extends TicTacToeRules {
        public Four() {
            super(4);
        }
    }

    public static class Five extends TicTacToeRules {
        public Five() {
            super(5);
        }
    }

    public static class Six extends TicTacToeRules {
        public Six() {
            super(6);
        }
    }

    private static final ImmutableList<ImmutableList<Direction>> COUNTING_DIRECTION = ImmutableList.of(
            ImmutableList.of(Direction.DOWN, Direction.UP),
            ImmutableList.of(Direction.RIGHT, Direction.LEFT),
            ImmutableList.of(Direction.UPPER_LEFT, Direction.LOWER_RIGHT),
            ImmutableList.of(Direction.UPPER_RIGHT, Direction.LOWER_LEFT)
    );

    private final int toWin;

    private TicTacToeRules(int toWin) {
        this.toWin = toWin;
    }

    @Override
    public Board prepareBoard() {
        return Board.square((int) Math.round(toWin * 2.5));
    }

    @Override
    public Deck prepareDeck(Session session, Player player) {
        if (!session.getPlayers().contains(player)) {
            throw new IllegalArgumentException("Player " + player + " does not play this game!");
        }
        if (session.getPlayers().indexOf(player) == 0) {
            return Deck.ofSame(TicTacToeTokens.CROSS, session.getBoard().getWidth() * session.getBoard().getHeight());
        }
        return Deck.ofSame(TicTacToeTokens.CIRCLE, session.getBoard().getWidth() * session.getBoard().getHeight());
    }

    @Override
    public EvaluationResult evaluate(Session session, TokenPlacement placement) {
        EvaluationResult.Builder builder = EvaluationResult.builder(placement);

        for (ImmutableList<Direction> directions : COUNTING_DIRECTION) {
            int count = countAround(session, placement, directions);
            if (count >= toWin) {
                return builder.updateScore(placement.getPlayer(), 1).finishGame().build();
            }
        }

        for (Player player : session.getPlayers()) {
            if (!player.equals(placement.getPlayer())) {
                builder.nextPlayer(player);
                if (session.getDeck(player).getPlayableTokens().size() == 0) {
                    return builder.finishGame().build();
                }
            }
        }

        if (session.getBoard().isCompletelyFilled()) {
            return builder.finishGame().build();
        }

        return builder.build();
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    public String getDescription() {
        return "Player who first place " + toWin + " tokens in a vertical, horizontal or diagonal row wins";
    }

    private int countAround(Session session, TokenPlacement placement, ImmutableList<Direction> directions) {
        Token expected = placement.getToken();

        int count = 1;

        for  (Direction direction : directions) {
            Position position = placement.getDestination().at(direction);

            while (session.getBoard().getTokenPlacement(position).map(other -> expected.equals(other.getToken())).orElse(false)) {
                count++;
                position = position.at(direction);
            }
        }

        return count;
    }
}
