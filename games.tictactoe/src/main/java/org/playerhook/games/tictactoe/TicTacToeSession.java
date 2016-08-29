package org.playerhook.games.tictactoe;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.Iterables;
import org.playerhook.games.api.*;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.*;

public class TicTacToeSession implements LocalSession {

    private static final String GAME_TITLE = "Tic Tac Toe";
    private static final Game GAME = Game.of(GAME_TITLE, 2);

    private static final ImmutableList<ImmutableList<Direction>> COUNTING_DIRECTION = ImmutableList.of(
      ImmutableList.of(Direction.DOWN, Direction.UP),
      ImmutableList.of(Direction.RIGHT, Direction.LEFT),
      ImmutableList.of(Direction.UPPER_LEFT, Direction.LOWER_RIGHT),
      ImmutableList.of(Direction.UPPER_RIGHT, Direction.LOWER_LEFT)
    );

    private final int toWin;

    private Board board;
    private Status status = Status.WAITING;
    private List<Player> players = new ArrayList<>();
    private List<Move> moves = new ArrayList<>();
    private Player activePlayer;
    private Player winner;

    private final PublishSubject<SessionUpdate> subject = PublishSubject.create();

    public TicTacToeSession(int size, int toWin) {
        if (toWin > size) {
            throw new IllegalArgumentException("The number of crosses or circles in row to win (" + toWin + ") cannot be less than the grid size (" + size + ")!");
        }
        this.toWin = toWin;
        this.board = Board.square(size);
    }

    @Override
    public Game getGame() {
        return GAME;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public List<Player> getPlayers() {
        return ImmutableList.copyOf(players);
    }

    @Override
    public Optional<Player> getPlayerOnTurn() {
        return Optional.ofNullable(activePlayer);
    }

    @Override
    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    @Override
    public List<Move> getPlayedMoves() {
        return ImmutableList.copyOf(moves);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Deck getDeck(Player player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player " + player + " does not play this game!");
        }
        if (players.indexOf(player) == 0) {
            return Deck.of(TicTacToeTokens.CROSS);
        }
        return Deck.of(TicTacToeTokens.CIRCLE);
    }

    @Override
    public int getScore(Player player) {
        if (!Status.FINISHED.equals(status)) {
            return 0;
        }
        if (player.equals(winner)) {
            return 1;
        }
        return 0;
    }

    @Override
    public void play(TokenPlacement placement) {
        if (Status.WAITING.equals(getStatus())) {
            move(Move.of(placement, RuleViolation.Default.GAME_NOT_STARTED_YET));
            return;
        }

        Optional<Player> playerOnTurn = getPlayerOnTurn();
        if (playerOnTurn.isPresent() && !playerOnTurn.get().equals(placement.getPlayer())) {
            move(Move.of(placement, RuleViolation.Default.NOT_YOUR_TURN));
            return;
        }
        if (!getDeck(placement.getPlayer()).getPlayableTokens().contains(placement.getToken())) {
            move(Move.of(placement, RuleViolation.Default.ILLEGAL_TOKEN));
        }
        if (getBoard().getTokenPlacement(placement.getPosition()).isPresent()) {
            move(Move.of(placement, RuleViolation.Default.POSITION_ALREADY_TAKEN));
            return;
        }
        if (!getBoard().contains(placement.getPosition())) {
            move(Move.of(placement, RuleViolation.Default.TOKEN_NOT_ALLOWED_ON_GIVEN_POSITION));
            return;
        }

        board = board.place(placement);
        move(Move.of(placement));

        for (ImmutableList<Direction> directions : COUNTING_DIRECTION) {
            int count = countAround(placement, directions);
            if (count >= toWin) {
                winner = placement.getPlayer();
                changeState(Status.FINISHED);
                return;
            }
        }

        for (Player player : players) {
            if (!player.equals(placement.getPlayer())) {
                activePlayer = player;
                if (getDeck(activePlayer).getPlayableTokens().size() == 0) {
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
            Position position = placement.getPosition().at(direction);

            while (board.getTokenPlacement(position).map(other -> expected.equals(other.getToken())).orElse(false)) {
                count++;
                position = position.at(direction);
            }
        }

        return count;
    }

    @Override
    public void join(Player newPlayer) {
        if (players.contains(newPlayer)) {
            throw new IllegalStateException("Already playing this game!");
        }
        if (hasEmptySeat()) {
            players.add(newPlayer);
            subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.PLAYER));
            return;
        }
        throw new IllegalStateException("Not more seats");
    }

    @Override
    public void start() {
        if (canStart()) {
            activePlayer = Iterables.getFirst(players, null);
            changeState(Status.IN_PROGRESS);
            return;
        }
        throw new IllegalStateException("Not enough players");
    }

    @Override
    public boolean hasEmptySeat() {
        return players.size() < getGame().getMaxPlayers();
    }

    @Override
    public boolean canStart() {
        return players.size() >= getGame().getMinPlayers() && players.size() <= getGame().getMaxPlayers();
    }

    @Override
    public Observable<SessionUpdate> asObservable() {
        return subject;
    }

    private void changeState(Status status) {
        this.status = status;
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.STATUS));
        if (status == Status.FINISHED) {
            subject.onCompleted();
        }
    }

    private void move(Move move) {
        if (moves.size() > 0) {
            Move last = Iterables.getLast(moves);
            if (last.getRuleViolation().isPresent() && last.getTokenPlacement().getPlayer().equals(move.getTokenPlacement().getPlayer())) {
                moves.remove(moves.size() - 1);
            }
        }
        moves.add(move);
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.MOVE));
    }
}
