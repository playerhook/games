package org.playerhook.games.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;

import static org.playerhook.games.util.MapSerializable.*;

final class RemoteSession implements Session {

    private final Board board;
    private final Game game;
    private final Status status;
    private final ImmutableList<Player> players;
    private final Player activePlayer;
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


    public Game getGame() {
        return game;
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

    @Override
    public String toString() {
        return "Session: " + url + " of " + getGame();
    }

}
