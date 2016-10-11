package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.collect.*;
import com.google.common.io.BaseEncoding;
import org.playerhook.games.util.MapSerializable;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.net.URL;
import java.util.*;
import java.util.Optional;

final class DefaultLocalSession implements LocalSession {

    static DefaultLocalSession newSession(Game game, URL url) {
        return new DefaultLocalSession(game, url);
    }

    private final DefaultSession delegate;
    private final String key;

    private DefaultLocalSession(Game game, URL url) {
        this.delegate = new DefaultSession(
                1,
                game.getRules().prepareBoard(),
                game,
                Status.WAITING,
                ImmutableList.of(),
                null,
                url,
                ImmutableList.of(),
                ImmutableMap.of(),
                ImmutableMap.of()
        );

        this.key = null;
    }

    /**
     * Update constructor.
     */
    private DefaultLocalSession(
            long version,
            Game game,
            Board board,
            URL url,
            ImmutableMap<Player, Deck> decks,
            ImmutableMap<Player, Integer> scores,
            ImmutableList<Player> players,
            ImmutableList<Move> moves,
            Status status,
            Player activePlayer,
            String key
    ) {
        this.delegate = new DefaultSession(
                version,
                board,
                game,
                status,
                players,
                activePlayer,
                url,
                moves,
                decks,
                scores
        );
        this.key = key;
    }

    @Override
    public LocalSession signWith(String privateKey) {
        return new DefaultLocalSession(
                delegate.getRound() + 1,
                delegate.getGame(),
                delegate.getBoard(),
                delegate.getURL().orElse(null),
                delegate.getDecks(),
                delegate.getScores(),
                delegate.getPlayers(),
                delegate.getMoves(),
                delegate.getStatus(),
                delegate.getPlayerOnTurn().orElse(null),
                privateKey
        );

    }

    public Game getGame() {
        return delegate.getGame();
    }

    public Optional<URL> getURL() {
        return delegate.getURL();
    }

    public Board getBoard() {
        return delegate.getBoard();
    }

    public ImmutableList<Player> getPlayers() {
        return delegate.getPlayers();
    }

    public Optional<Player> getPlayerOnTurn() {
        return delegate.getPlayerOnTurn();
    }

    public ImmutableList<Move> getMoves() {
        return delegate.getMoves();
    }

    public Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public LocalSession join(Player newPlayer) {
        if (!delegate.getStatus().equals(Status.WAITING)) {
            throw new IllegalStateException("The game has already started!");
        }
        if (delegate.getPlayers().contains(newPlayer)) {
            throw new IllegalStateException("Already playing this game!");
        }
        if (hasEmptySeat()) {
            return new DefaultLocalSession(
                    delegate.getRound() + 1,
                    getGame(),
                    getBoard(),
                    getURL().orElse(null),
                    delegate.getDecks(),
                    delegate.getScores(),
                    ImmutableList.<Player>builder().addAll(getPlayers()).add(newPlayer).build(),
                    delegate.getMoves(),
                    getStatus(),
                    delegate.getPlayerOnTurn().orElse(null),
                    key
            );
        }
        throw new IllegalStateException("No more seats!");
    }

    @Override
    public LocalSession start() {
        if (!getStatus().equals(Status.WAITING)) {
            throw new IllegalStateException("The game has already started!");
        }
        if (canStart()) {
            return new DefaultLocalSession(
                    delegate.getRound() + 1,
                    getGame(),
                    getBoard(),
                    getURL().orElse(null),
                    prepareDeck(),
                    delegate.getScores(),
                    getPlayers(),
                    getMoves(),
                    Status.IN_PROGRESS,
                    Iterables.getFirst(getPlayers(), null),
                    key
            );
        }
        throw new IllegalStateException("Not enough players!");
    }

    @Override
    public LocalSession suspend() {
        if (!getStatus().equals(Status.IN_PROGRESS)) {
            throw new IllegalStateException("The game is not in progress!");
        }
        return new DefaultLocalSession(
                delegate.getRound() + 1,
                getGame(),
                getBoard(),
                getURL().orElse(null),
                delegate.getDecks(),
                delegate.getScores(),
                getPlayers(),
                getMoves(),
                Status.SUSPENDED,
                delegate.getPlayerOnTurn().orElse(null),
                key
        );
    }

    private ImmutableMap<Player, Deck> prepareDeck() {
        ImmutableMap.Builder<Player, Deck> newDecks = ImmutableMap.builder();

        for (Player player: getPlayers()) {
            newDecks.put(player, getGame().getRules().prepareDeck(this, player));
        }

        return newDecks.build();
    }

    @Override
    public Deck getDeck(Player player) {
        return delegate.getDecks().get(player);
    }

    @Override
    public int getScore(Player player) {
        return delegate.getScore(player);
    }

    @Override
    public LocalSession play(TokenPlacement placement) {
        Rules.EvaluationResult genericChecks = doGenericChecks(placement);

        if (genericChecks.getMove().getRuleViolation().isPresent()) {
            return new DefaultLocalSession(
                    delegate.getRound() + 1,
                    getGame(),
                    getBoard(),
                    getURL().orElse(null),
                    delegate.getDecks(),
                    delegate.getScores(),
                    getPlayers(),
                    ImmutableList.<Move>builder().addAll(getMoves()).add(genericChecks.getMove()).build(),
                    getStatus(),
                    getPlayerOnTurn().orElse(null),
                    key
            );
        }

        Rules.EvaluationResult result = getGame().getRules().evaluate(this, placement);

        Move move = result.getMove();

        if (move.getRuleViolation().isPresent()) {
            return new DefaultLocalSession(
                    delegate.getRound() + 1,
                    getGame(),
                    getBoard(),
                    getURL().orElse(null),
                    delegate.getDecks(),
                    delegate.getScores(),
                    getPlayers(),
                    ImmutableList.<Move>builder().addAll(getMoves()).add(result.getMove()).build(),
                    getStatus(),
                    getPlayerOnTurn().orElse(null),
                    key
            );
        }

        Board board = getBoard().place(placement);

        Map<Player, Integer> scores = Maps.newHashMap(delegate.getScores());

        for(Map.Entry<Player, Integer> scoreEntry : result.getScoreUpdates().entrySet()) {
            Integer nextScore = scores.getOrDefault(scoreEntry.getKey(), 0);
            nextScore += scoreEntry.getValue();
            scores.put(scoreEntry.getKey(), nextScore);
        }

        Player activePlayer = getPlayerOnTurn().orElse(null);
        if (!result.getNextPlayer().equals(activePlayer)) {
            activePlayer = result.getNextPlayer();
        }

        Map<Player, Deck> decks = Maps.newHashMap(delegate.getDecks());
        if (!placement.getSource().isPresent()) {
            decks.put(placement.getPlayer(), decks.get(placement.getPlayer()).remove(placement.getToken()));
        }

        return new DefaultLocalSession(
                delegate.getRound() + 1,
                getGame(),
                board,
                getURL().orElse(null),
                ImmutableMap.copyOf(decks),
                ImmutableMap.copyOf(scores),
                getPlayers(),
                ImmutableList.<Move>builder().addAll(getMoves()).add(result.getMove()).build(),
                result.getNextStatus().orElse(getStatus()),
                activePlayer,
                key
        );
    }

    private Rules.EvaluationResult doGenericChecks(TokenPlacement placement) {
        if (getStatus().equals(Status.FINISHED)) {
            return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.GAME_OVER).build();
        }

        if (Status.WAITING.equals(getStatus())) {
            return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.GAME_NOT_STARTED_YET).build();
        }

        if (key != null) {
            if (!placement.getKey().isPresent()) {
                return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.KEY_MISSING).build();
            }
            if (!placement.getKey().get().equals(generateUserKey(placement.getPlayer().getUsername(), key + ":" + delegate.getRound()))) {
                return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.KEY_MISMATCH).build();
            }
        }

        Optional<Player> playerOnTurn = getPlayerOnTurn();
        if (playerOnTurn.isPresent() && !playerOnTurn.get().equals(placement.getPlayer())) {
            return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.NOT_YOUR_TURN).build();
        }
        if (!Token.contains(getDeck(placement.getPlayer()).getPlayableTokens(), placement.getToken())) {
            return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.ILLEGAL_TOKEN).build();
        }
        if (getBoard().getTokenPlacement(placement.getDestination()).isPresent()) {
            return Rules.EvaluationResult.builder(placement).ruleViolation(RuleViolation.Default.POSITION_ALREADY_TAKEN).build();
        }
        if (!getBoard().contains(placement.getDestination())) {
            return Rules.EvaluationResult.builder(placement)
                    .ruleViolation(RuleViolation.Default.TOKEN_NOT_ALLOWED_ON_GIVEN_POSITION).build();
        }
        return Rules.EvaluationResult.builder(placement).build();
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

        DefaultSession defaultSession = DefaultSession.load(session);

        String key = MapSerializable.loadString(payload, "key");

        return new DefaultLocalSession(
                defaultSession.getRound(),
                defaultSession.getGame(),
                defaultSession.getBoard(),
                defaultSession.getURL().orElse(null),
                defaultSession.getDecks(),
                defaultSession.getScores(),
                defaultSession.getPlayers(),
                defaultSession.getMoves(),
                defaultSession.getStatus(),
                defaultSession.getPlayerOnTurn().orElse(null),
                key
        );
    }

    @Override public Map<String, Object> toMap(PrivacyLevel level) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        if (key != null && PrivacyLevel.INTERNAL.equals(level)) {
            builder.put("key", key);
        }

        builder.putAll(delegate.toMap(level));

        return builder.build();
    }

    private static final int ITERATIONS = 20*1000;
    private static final int DESIRED_KEY_LEN = 64;

    @Override
    public Optional<String> getKey(Player player) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(generateUserKey(player.getUsername(), key + ":" + delegate.getRound()));
    }

    private static String generateUserKey(String username, String privateKey) {
        SecretKeyFactory f;
        try {
            f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey key = f.generateSecret(new PBEKeySpec(username.toCharArray(), privateKey.getBytes(), ITERATIONS, DESIRED_KEY_LEN));
            return BaseEncoding.base64().encode(key.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Problems generating player key", e);
        }
    }


    @Override
    public TokenPlacement newPlacement(Token token, Player player, Position source, Position destination) {
        return TokenPlacement.create(token, player, source, destination, getKey(player).orElse(null));
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultLocalSession session = (DefaultLocalSession) o;
        return com.google.common.base.Objects.equal(delegate, session.delegate) &&
                Objects.equal(key, session.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate, key);
    }
    //CHECKSTYLE:ON



}
