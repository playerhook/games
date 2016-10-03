package org.playerhook.games.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import rx.subjects.PublishSubject;

import java.net.URL;
import java.util.*;

final class DefaultLocalSession implements LocalSession {

    private final Map<Player, Deck> decks = new HashMap<>();
    private final Map<Player, Integer> scores = new HashMap<>();
    private final PublishSubject<SessionUpdate> subject = PublishSubject.create();
    private Board board;
    private Game game;
    private URL url;
    private Status status = Status.WAITING;
    private List<Player> players = new ArrayList<>();
    private Player activePlayer;
    private List<Move> moves = new ArrayList<>();

    public Game getGame() {
        return game;
    }

    public Optional<URL> getURL() {
        return Optional.ofNullable(url);
    }

    public Board getBoard() {
        return board;
    }

    public ImmutableList<Player> getPlayers() {
        return ImmutableList.copyOf(players);
    }

    public Optional<Player> getPlayerOnTurn() {
        return Optional.ofNullable(activePlayer);
    }

    public ImmutableList<Move> getPlayedMoves() {
        return ImmutableList.copyOf(moves);
    }

    public Status getStatus() {
        return status;
    }

    public PublishSubject<SessionUpdate> asObservable() {
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
        moves.add(move);
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.MOVE));
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
            this.activePlayer = Iterables.getFirst(getPlayers(), null);
            changeState(Status.IN_PROGRESS);
            return;
        }
        throw new IllegalStateException("Not enough players!");
    }

    private boolean doGenericChecks(TokenPlacement placement) {
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

    DefaultLocalSession(Game game, Board board, URL url) {
        this.game = Preconditions.checkNotNull(game, "Game cannot be null");
        this.board = Preconditions.checkNotNull(board, "Board cannot be null");
        this.url = url;
    }

    @Override
    public Deck getDeck(Player player) {
        Deck deck = decks.getOrDefault(player, getGame().getRules().prepareDeck(this, player));
        decks.put(player, deck);
        return deck;
    }

    @Override
    public int getScore(Player player) {
        return scores.getOrDefault(player, 0);
    }

    @Override
    public void play(TokenPlacement placement) {
        if (doGenericChecks(placement)) {
            return;
        }

        Rules.EvaluationResult result = getGame().getRules().evaluate(this, placement);

        Move move = result.getMove();

        if (move.getRuleViolation().isPresent()) {
            return;
        }

        this.board = getBoard().place(placement);
        move(move);

        for(Map.Entry<Player, Integer> scoreEntry : result.getScoreUpdates().entrySet()) {
            Integer nextScore = scores.getOrDefault(scoreEntry.getKey(), 0);
            nextScore += scoreEntry.getValue();
            scores.put(scoreEntry.getKey(), nextScore);
        }

        if (!result.getNextStatus().map(status -> status.equals(getStatus())).orElse(false)) {
            result.getNextStatus().ifPresent(this::changeState);
        }

        if (!result.getNextPlayer().equals(activePlayer)) {
            this.activePlayer = result.getNextPlayer();
        }
    }


}
