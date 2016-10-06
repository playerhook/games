package org.playerhook.games.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.playerhook.games.util.MapSerializable;
import rx.subjects.PublishSubject;

import java.net.URL;
import java.util.*;

final class DefaultLocalSession implements LocalSession {

    private final Map<Player, Deck> decks = new HashMap<>();
    private final Map<Player, Integer> scores = new HashMap<>();
    private final PublishSubject<SessionUpdate> subject = PublishSubject.create();
    private final Game game;
    private final URL url;

    private final List<Player> players = new ArrayList<>();
    private final List<Move> moves = new ArrayList<>();

    private Board board;
    private Status status = Status.WAITING;
    private Player activePlayer;

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

    DefaultLocalSession(Game game, URL url) {
        this.game = Preconditions.checkNotNull(game, "Game cannot be null");
        this.board = game.getRules().prepareBoard();
        this.url = url;
    }

    /**
     * Deserialization constructor.
     */
    DefaultLocalSession(Game game,
                        Board board,
                        URL url,
                        Map<Player, Deck> decks,
                        Map<Player, Integer> scores,
                        List<Player> players,
                        List<Move> moves,
                        Status status,
                        Player activePlayer) {
        this.game = game;
        this.board = board;
        this.url = url;
        this.status = status;
        this.activePlayer = activePlayer;

        this.decks.putAll(decks);
        this.scores.putAll(scores);
        this.players.addAll(players);
        this.moves.addAll(moves);
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

        for(Map.Entry<Player, Integer> scoreEntry : result.getScoreUpdates().entrySet()) {
            Integer nextScore = scores.getOrDefault(scoreEntry.getKey(), 0);
            nextScore += scoreEntry.getValue();
            scores.put(scoreEntry.getKey(), nextScore);
        }

        if (!result.getNextPlayer().equals(activePlayer)) {
            this.activePlayer = result.getNextPlayer();
        }

        if (!result.getNextStatus().map(status -> status.equals(getStatus())).orElse(false)) {
            result.getNextStatus().ifPresent(this::changeState);
        }

        move(move);
    }

    @Override
    public String toString() {
        Optional<URL> url = getURL();
        if (url.isPresent()) {
            return "Session: " + url.get() + " of " + getGame();
        }
        return "Unidentified session of " + getGame();
    }

    static LocalSession load(Object session) {
        if (!(session instanceof Map)) {
            throw new IllegalArgumentException("Cannot load session from " + session);
        }

        Map<String, Object> payload = (Map<String, Object>) session;

        URL url = MapSerializable.loadURL(payload, "url");
        Game game = Game.load(payload.getOrDefault("game", null));
        Board board = Board.load(payload.getOrDefault("board", null));
        Player activePlayer = Player.load(payload.getOrDefault("playerOnTurn", null));
        List<Player> players = MapSerializable.loadList(payload.getOrDefault("players", Collections.emptyList()), Player::load);
        List<Move> moves = MapSerializable.loadList(payload.getOrDefault("playedMoves", Collections.emptyList()), Move::load);
        Status status = Status.valueOf(payload.getOrDefault("status", Status.WAITING).toString());
        ImmutableMap<Player, Integer> scores = loadScores(players, payload.getOrDefault("scores", Collections.emptyList()));
        ImmutableMap<Player, Deck> decks = loadDecks(players, payload.getOrDefault("decks", Collections.emptyList()));

        return new DefaultLocalSession(
                game,
                board,
                url,
                decks,
                scores,
                players,
                moves,
                status,
                activePlayer
        );
    }

    private static Player findPlayer(List<Player> players, String username) {
        return players.stream()
                .filter(player -> player.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not present: " + username));
    }

    private static ImmutableMap<Player, Integer> loadScores(List<Player> players, Object map) {
        if (!(map instanceof Map)) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Player, Integer> builder = ImmutableMap.builder();

        for (Map.Entry<String, Integer> o : ((Map<String, Integer>) map).entrySet()) {
            builder.put(findPlayer(players, o.getKey()), o.getValue());
        }

        return builder.build();
    }

    private static ImmutableMap<Player, Deck> loadDecks(List<Player> players, Object map) {
        if (!(map instanceof Map)) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Player, Deck> builder = ImmutableMap.builder();

        for (Map.Entry<String, Object> o : ((Map<String, Object>) map).entrySet()) {
            builder.put(findPlayer(players, o.getKey()), Deck.load(o.getValue()));
        }

        return builder.build();
    }
}
