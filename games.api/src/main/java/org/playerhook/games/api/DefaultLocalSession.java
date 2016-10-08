package org.playerhook.games.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import org.playerhook.games.util.MapSerializable;
import rx.subjects.PublishSubject;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

final class DefaultLocalSession implements LocalSession {

    private static final ConcurrentMap<URL, DefaultLocalSession> ACTIVE_SESSIONS = new MapMaker()
            .weakValues()
            .makeMap();

    static DefaultLocalSession get(Game game, URL url) {
        if (url != null) {
            DefaultLocalSession existing = ACTIVE_SESSIONS.get(url);
            if (existing != null) {
                throw new IllegalArgumentException("Session with url " + url + " already exists!");
            }
        }

        DefaultLocalSession session = new DefaultLocalSession(game, url);
        if (url != null) {
            ACTIVE_SESSIONS.put(url, session);
        }
        return session;
    }

    static DefaultLocalSession find(URL url) {
        return ACTIVE_SESSIONS.get(url);
    }

    private final Map<Player, Deck> decks = new HashMap<>();
    private final Map<Player, Integer> scores = new HashMap<>();
    private final PublishSubject<SessionUpdate> subject = PublishSubject.create();
    private final Game game;
    private final URL url;

    private final List<Player> players = new ArrayList<>();
    private final List<Move> moves = new ArrayList<>();

    private final Lock turnLock = new ReentrantLock();
    private final AtomicInteger version;

    private Board board;
    private Status status = Status.WAITING;
    private Player activePlayer;
    private String key;

    private DefaultLocalSession(Game game, URL url) {
        this.game = Preconditions.checkNotNull(game, "Game cannot be null");
        this.board = game.getRules().prepareBoard();
        this.url = url;
        this.version = new AtomicInteger(1);
    }

    /**
     * Deserialization constructor.
     */
    private DefaultLocalSession(
            Integer version,
            Game game,
            Board board,
            URL url,
            Map<Player, Deck> decks,
            Map<Player, Integer> scores,
            List<Player> players,
            List<Move> moves,
            Status status,
            Player activePlayer,
            String key
    ) {
        this.game = game;
        this.board = board;
        this.url = url;
        this.status = status;
        this.activePlayer = activePlayer;

        this.decks.putAll(decks);
        this.scores.putAll(scores);
        this.players.addAll(players);
        this.moves.addAll(moves);
        this.version = new AtomicInteger(version);
        this.key = key;
    }

    @Override
    public void signWith(String privateKey) {
        this.key = privateKey;
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
        version.incrementAndGet();
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.STATUS));
        if (status == Status.FINISHED) {
            subject.onCompleted();
            if (url != null) {
                ACTIVE_SESSIONS.remove(url);
            }
        }
    }

    private void move(Move move) {
        moves.add(move);
        version.incrementAndGet();
        subject.onNext(SessionUpdate.of(this, SessionUpdateType.Default.MOVE));
    }

    @Override
    public void join(Player newPlayer) {
        if (!status.equals(Status.WAITING)) {
            throw new IllegalStateException("The game has already started!");
        }
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
        if (!status.equals(Status.WAITING)) {
            throw new IllegalStateException("The game has already started!");
        }
        if (canStart()) {
            this.activePlayer = Iterables.getFirst(getPlayers(), null);
            changeState(Status.IN_PROGRESS);
            return;
        }
        throw new IllegalStateException("Not enough players!");
    }

    private boolean genericChecksFails(TokenPlacement placement) {
        if (Status.WAITING.equals(getStatus())) {
            move(Move.to(placement, RuleViolation.Default.GAME_NOT_STARTED_YET));
            return true;
        }

        if (key != null) {
            if (!placement.getKey().isPresent()) {
                move(Move.to(placement, RuleViolation.Default.KEY_MISSING));
                return true;
            }
            if (!placement.getKey().get().equals(generateUserKey(placement.getPlayer().getUsername(), key))) {
                move(Move.to(placement, RuleViolation.Default.KEY_MISMATCH));
                return true;
            }
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
        turnLock.lock();
        if (status.equals(Status.FINISHED)) {
            throw new IllegalStateException("The game has already finished!");
        }
        if (genericChecksFails(placement)) {
            turnLock.unlock();
            return;
        }

        Rules.EvaluationResult result = getGame().getRules().evaluate(this, placement);

        Move move = result.getMove();

        if (move.getRuleViolation().isPresent()) {
            turnLock.unlock();
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
        turnLock.unlock();
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

        Integer version = MapSerializable.loadInteger(payload, "version");
        Game game = Game.load(payload.getOrDefault("game", null));
        Board board = Board.load(payload.getOrDefault("board", null));
        Player activePlayer = Player.load(payload.getOrDefault("playerOnTurn", null));
        List<Player> players = MapSerializable.loadList(payload.getOrDefault("players", Collections.emptyList()), Player::load);
        List<Move> moves = MapSerializable.loadList(payload.getOrDefault("playedMoves", Collections.emptyList()), Move::load);
        Status status = Status.valueOf(payload.getOrDefault("status", Status.WAITING).toString());
        ImmutableMap<Player, Integer> scores = loadScores(players, payload.getOrDefault("scores", Collections.emptyList()));
        ImmutableMap<Player, Deck> decks = loadDecks(players, payload.getOrDefault("decks", Collections.emptyList()));
        String key = MapSerializable.loadString(payload, "key");

        if (url != null) {
            DefaultLocalSession existing = ACTIVE_SESSIONS.get(url);
            if (existing != null) {
                existing.update(
                        version,
                        game,
                        board,
                        decks,
                        scores,
                        players,
                        moves,
                        status,
                        activePlayer);
                return existing;
            }
        }

        DefaultLocalSession newSession = new DefaultLocalSession(
                version,
                game,
                board,
                url,
                decks,
                scores,
                players,
                moves,
                status,
                activePlayer,
                key
        );

        if (!newSession.getStatus().equals(Status.FINISHED) && newSession.getURL().isPresent()) {
            ACTIVE_SESSIONS.put(newSession.getURL().get(), newSession);
        }

        return newSession;
    }

    private void update(Integer version, Game game, Board board, ImmutableMap<Player, Deck> decks, ImmutableMap<Player, Integer> scores, List<Player> players, List<Move> moves, Status status, Player activePlayer) {
        if (version != null && version <= this.version.get()) {
            return;
        }

        if (!this.game.equals(game)) {
            throw new IllegalArgumentException("Trying to load existing session with different game. Original: " + getGame() + ", new: " + game);
        }

        if (!this.board.equals(board)) {
            this.board = board;
        }

        if (!this.decks.equals(decks)) {
            this.decks.clear();
            this.decks.putAll(decks);
        }

        if (!this.scores.equals(scores)) {
            this.scores.clear();
            this.scores.putAll(scores);
        }

        if (!this.players.equals(players)) {
            this.players.clear();
            this.players.addAll(players);
        }

        if (!this.moves.equals(moves)) {
            this.moves.clear();
            this.moves.addAll(moves);
        }

        if (this.status != null && !this.status.equals(status)) {
            this.status = status;
        }

        if (this.activePlayer != null && !this.activePlayer.equals(activePlayer)) {
            this.activePlayer = activePlayer;
        }
    }

    @Override public Map<String, Object> toMap(PrivacyLevel level) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        if (PrivacyLevel.INTERNAL.equals(level)) {
            builder.put("version", version.get());
        }

        if (key != null && PrivacyLevel.INTERNAL.equals(level)) {
            builder.put("key", key);
        }

        builder.put("game", getGame().toMap(level));
        builder.put("board", getBoard().toMap(level));
        builder.put("players", getPlayers().stream().map((player2) -> player2.toMap(level)).collect(Collectors.toList()));
        builder.put("scores", ImmutableMap.copyOf(getPlayers().stream().collect(Collectors.toMap(Player::getUsername, this::getScore))));
        builder.put("decks", ImmutableMap.copyOf(getPlayers().stream().collect(Collectors.toMap(Player::getUsername, (player1) -> getDeck(player1).toMap(level)))));
        builder.put("playedMoves", getPlayedMoves().stream().map((move) -> move.toMap(level)).collect(Collectors.toList()));
        builder.put("status", getStatus().name());

        getPlayerOnTurn().ifPresent(player -> builder.put("playerOnTurn", player.toMap(level)));
        getURL().ifPresent(s -> builder.put("url", s.toExternalForm()));

        return builder.build();
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

    private static final int ITERATIONS = 20*1000;
    private static final int DESIRED_KEY_LEN = 64;

    @Override
    public Optional<String> getKey(Player player) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(generateUserKey(player.getUsername(), key));
    }

    private static String generateUserKey(String username, String privateKey) {
        SecretKeyFactory f = null;
        try {
            f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey key = f.generateSecret(new PBEKeySpec(username.toCharArray(), privateKey.getBytes(), ITERATIONS, DESIRED_KEY_LEN));
            return new String(key.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Problems generating player key", e);
        }
    }
}
