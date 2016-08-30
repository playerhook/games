package org.playerhook.games.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import rx.subjects.PublishSubject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractSession implements LocalSession {


    private final PublishSubject<SessionUpdate> subject = PublishSubject.create();
    private Board board;
    private Game game;
    private URL url;
    private Status status = Status.WAITING;
    private List<Player> players = new ArrayList<>();
    private Player activePlayer;
    private Player winner;
    private List<Move> moves = new ArrayList<>();

    protected AbstractSession(Game game, Board board, URL url) {
        this.game = Preconditions.checkNotNull(game, "Game cannot be null");
        this.board = Preconditions.checkNotNull(board, "Board cannot be null");
        this.url = url;
    }

    public Game getGame() {
        return game;
    }

    public Optional<URL> getURL() {
        return Optional.ofNullable(url);
    }

    public Board getBoard() {
        return board;
    }

    protected void setBoard(Board board) {
        this.board = board;
    }

    public List<Player> getPlayers() {
        return ImmutableList.copyOf(players);
    }

    public Optional<Player> getPlayerOnTurn() {
        return Optional.ofNullable(activePlayer);
    }

    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    public List<Move> getPlayedMoves() {
        return ImmutableList.copyOf(moves);
    }

    public Status getStatus() {
        return status;
    }

    public PublishSubject<SessionUpdate> asObservable() {
        return subject;
    }

    protected void changeState(Status status) {
        this.status = status;
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.STATUS));
        if (status == Status.FINISHED) {
            subject.onCompleted();
        }
    }

    protected void move(Move move) {
        if (moves.size() > 0) {
            Move last = Iterables.getLast(moves);
            if (last.getRuleViolation().isPresent() && last.getTokenPlacement().getPlayer().equals(move.getTokenPlacement().getPlayer())) {
                moves.remove(moves.size() - 1);
            }
        }
        moves.add(move);
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.MOVE));
    }

    protected void setWinner(Player winner) {
        this.winner = winner;
    }

    protected void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer;
    }

    @Override
    public void join(Player newPlayer) {
        if (players.contains(newPlayer)) {
            throw new IllegalStateException("Already playing this game!");
        }
        if (hasEmptySeat()) {
            players.add(newPlayer);
            asObservable().onNext(SessionUpdate.of(this, SessionUpdateType.Default.PLAYER));
            return;
        }
        throw new IllegalStateException("No more seats!");
    }

    @Override
    public void start() {
        if (canStart()) {
            setActivePlayer(Iterables.getFirst(getPlayers(), null));
            changeState(Status.IN_PROGRESS);
            return;
        }
        throw new IllegalStateException("Not enough players!");
    }

    protected boolean doGenericChecks(TokenPlacement placement) {
        if (Status.WAITING.equals(getStatus())) {
            move(Move.to(placement, RuleViolation.Default.GAME_NOT_STARTED_YET));
            return true;
        }

        Optional<Player> playerOnTurn = getPlayerOnTurn();
        if (playerOnTurn.isPresent() && !playerOnTurn.get().equals(placement.getPlayer())) {
            move(Move.to(placement, RuleViolation.Default.NOT_YOUR_TURN));
            return true;
        }
        if (!getDeck(placement.getPlayer()).getPlayableTokens().contains(placement.getToken())) {
            move(Move.to(placement, RuleViolation.Default.ILLEGAL_TOKEN));
        }
        if (getBoard().getTokenPlacement(placement.getDestination()).isPresent()) {
            move(Move.to(placement, RuleViolation.Default.POSITION_ALREADY_TAKEN));
            return true;
        }
        if (!getBoard().contains(placement.getDestination())) {
            move(Move.to(placement, RuleViolation.Default.TOKEN_NOT_ALLOWED_ON_GIVEN_POSITION));
            return true;
        }
        return false;
    }

}
