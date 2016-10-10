package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.playerhook.games.util.MapSerializable.*;

final class DefaultSession implements Session {

    private final long version;
    private final Board board;
    private final Game game;
    private final Status status;
    private final ImmutableList<Player> players;
    private final Player activePlayer;
    private final URL url;
    private final ImmutableList<Move> moves;
    private final ImmutableMap<Player, Deck> decks;
    private final ImmutableMap<Player, Integer> scores;

    DefaultSession(long version, Board board, Game game, Status status, ImmutableList<Player> players,
                   Player activePlayer, URL url, ImmutableList<Move> moves, ImmutableMap<Player, Deck> decks,
                   ImmutableMap<Player, Integer> scores) {
        this.version = version;
        this.board = board;
        this.game = game;
        this.status = status;
        this.players = players;
        this.activePlayer = activePlayer;
        this.url = url;
        this.moves = moves;
        this.decks = decks;
        this.scores = scores;
    }

    static DefaultSession load(Object session) {
        if (!(session instanceof Map)) {
            throw new IllegalArgumentException("Cannot load session from " + session);
        }

        Map<String, Object> payload = (Map<String, Object>) session;

        ImmutableList<Player> players = loadList(payload.getOrDefault("players", Collections.emptyList()), Player::load);
        return new DefaultSession(
                loadLong(payload, "version"),
                Board.load(payload.getOrDefault("board", null)),
                Game.load(payload.getOrDefault("game", null)),
                Status.valueOf(payload.getOrDefault("status", Status.WAITING).toString()),
                players,
                Player.load(payload.getOrDefault("playerOnTurn", null)),
                loadURL(payload, "url"),
                loadList(payload.getOrDefault("playedMoves", Collections.emptyList()), Move::load),
                loadDecks(players, payload.getOrDefault("decks", Collections.emptyList())),
                loadScores(players, payload.getOrDefault("scores", Collections.emptyList()))
        );
    }

    private static Player findPlayer(ImmutableList<Player> players, String username) {
        return players.stream()
            .filter(player -> player.getUsername().equals(username))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not present: " + username));
    }

    private static ImmutableMap<Player, Integer> loadScores(ImmutableList<Player> players, Object map) {
        if (!(map instanceof Map)) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Player, Integer> builder = ImmutableMap.builder();

        for (Map.Entry<String, Integer> o : ((Map<String, Integer>) map).entrySet()) {
            builder.put(findPlayer(players, o.getKey()), o.getValue());
        }

        return builder.build();
    }

    private static ImmutableMap<Player, Deck> loadDecks(ImmutableList<Player> players, Object map) {
        if (!(map instanceof Map)) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Player, Deck> builder = ImmutableMap.builder();

        for (Map.Entry<String, Object> o : ((Map<String, Object>) map).entrySet()) {
            builder.put(findPlayer(players, o.getKey()), Deck.load(o.getValue()));
        }

        return builder.build();
    }


    public Game getGame() {
        return game;
    }

    public Board getBoard() {
        return board;
    }

    public ImmutableList<Player> getPlayers() {
        return players;
    }

    public Optional<Player> getPlayerOnTurn() {
        return Optional.ofNullable(activePlayer);
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public Optional<URL> getURL() {
        return Optional.ofNullable(url);
    }

    @Override
    public Deck getDeck(Player player) {
        return decks.getOrDefault(player, Deck.of());
    }

    @Override
    public int getScore(Player player) {
        return scores.getOrDefault(player, 0);
    }

    @Override
    public String toString() {
        return "Session: " + url + " of " + getGame();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Map<String, Object> toMap(PrivacyLevel level) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("version", version);
        builder.put("game", getGame().toMap(level));
        builder.put("board", getBoard().toMap(level));
        builder.put("players", getPlayers().stream().map((player2) -> player2.toMap(level)).collect(Collectors.toList()));
        builder.put("scores", ImmutableMap.copyOf(getPlayers().stream().collect(Collectors.toMap(Player::getUsername, this::getScore))));
        builder.put("decks", ImmutableMap.copyOf(getPlayers().stream().collect(Collectors.toMap(Player::getUsername, (player1) -> getDeck(player1).toMap(level)))));
        builder.put("playedMoves", getMoves().stream().map((move) -> move.toMap(level)).collect(Collectors.toList()));
        builder.put("status", getStatus().name());

        getPlayerOnTurn().ifPresent(player -> builder.put("playerOnTurn", player.toMap(level)));
        getURL().ifPresent(s -> builder.put("url", s.toExternalForm()));

        return builder.build();
    }

    public ImmutableList<Move> getMoves() {
        return moves;
    }

    ImmutableMap<Player, Deck> getDecks() {
        return decks;
    }

    ImmutableMap<Player, Integer> getScores() {
        return scores;
    }

    //CHECKSTYLE:OFF

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultSession that = (DefaultSession) o;
        return version == that.version &&
                com.google.common.base.Objects.equal(board, that.board) &&
                Objects.equal(game, that.game) &&
                status == that.status &&
                Objects.equal(players, that.players) &&
                Objects.equal(activePlayer, that.activePlayer) &&
                Objects.equal(url, that.url) &&
                Objects.equal(moves, that.moves) &&
                Objects.equal(decks, that.decks) &&
                Objects.equal(scores, that.scores);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version, board, game, status, players, activePlayer, url, moves, decks, scores);
    }

    //CHECKSTYLE:ON

}
