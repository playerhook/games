package org.playerhook.games.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class RemoteSession implements Session {

    private final Board board;
    private final Game game;
    private final Status status;
    private final ImmutableList<Player> players;
    private final Player activePlayer;
    private final Player winner;
    private final URL url;
    private final ImmutableList<Move> moves;
    private final ImmutableMap<Player, Deck> decks;
    private final ImmutableMap<Player, Integer> scores;
    private final BiConsumer<URL, TokenPlacement> onPlay;

    RemoteSession(Object session, BiConsumer<URL, TokenPlacement> onPlay) {
        if (!(session instanceof Map)) {
            throw new IllegalArgumentException("Cannot load session from " + session);
        }

        Map<String, Object> payload = (Map<String, Object>) session;

        this.url = loadURL(payload, "url");

        if (this.url == null) {
            throw new IllegalArgumentException("Remote Session must has the URL specified");
        }

        this.game = Game.load(payload.getOrDefault("game", null));
        this.board = Board.load(payload.getOrDefault("board", null));
        this.activePlayer = Player.load(payload.getOrDefault("playerOnTurn", null));
        this.winner = Player.load(payload.getOrDefault("winner", null));
        this.players = loadList(payload.getOrDefault("players", Collections.emptyList()), Player::load);
        this.moves = loadList(payload.getOrDefault("playedMoves", Collections.emptyList()), Move::load);
        this.status = Status.valueOf(payload.getOrDefault("status", Status.WAITING).toString());
        this.onPlay = onPlay;

        this.scores = loadScores(payload.getOrDefault("scores", Collections.emptyList()));
        this.decks = loadDecks(payload.getOrDefault("decks", Collections.emptyList()));
    }

    private Player findPlayer(String username) {
        return players.stream()
            .filter(player -> player.getUsername().equals(username))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Player not present: " + username));
    }

    private ImmutableMap<Player, Integer> loadScores(Object map) {
        if (!(map instanceof Map)) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Player, Integer> builder = ImmutableMap.builder();

        for (Map.Entry<String, Integer> o : ((Map<String, Integer>) map).entrySet()) {
            builder.put(findPlayer(o.getKey()), o.getValue());
        }

        return builder.build();
    }

    private ImmutableMap<Player, Deck> loadDecks(Object map) {
        if (!(map instanceof Map)) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Player, Deck> builder = ImmutableMap.builder();

        for (Map.Entry<String, Object> o : ((Map<String, Object>) map).entrySet()) {
            builder.put(findPlayer(o.getKey()), Deck.load(o.getValue()));
        }

        return builder.build();
    }

    static <T> ImmutableList<T> loadList(Object list, Function<Object, T> loader) {
        if (!(list instanceof Iterable)) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<T> builder = ImmutableList.builder();

        for (Object payload : (Iterable) list) {
            builder.add(loader.apply(payload));
        }
        return builder.build();
    }


    static int loadInteger(Map<String, Object> map, String property) {
        return Integer.valueOf(Optional.ofNullable(map.get(property)).map(Object::toString).orElseThrow(() -> new IllegalArgumentException(property + " is missing!")));
    }

    static String loadString(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(Object::toString).orElse(null);
    }

    static Instant loadInstant(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(Object::toString).map(Long::parseLong).map(Instant::ofEpochMilli).orElseThrow(() -> new IllegalArgumentException("No " + property + "  for " + map));
    }

    static URL loadURL(Map<String, Object> map, String property) {
        return Optional.ofNullable(map.get(property)).map(o -> {
            try {
                return new URL(o.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error parsing the URL", e);
            }
        }).orElse(null);
    }

    public Game getGame() {
        return game;
    }

    public Board getBoard() {
        return board;
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
    public void play(TokenPlacement placement) {
        onPlay.accept(url, placement);
    }

}
