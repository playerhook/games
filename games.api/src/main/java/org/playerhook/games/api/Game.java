package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

public final class Game implements MapSerializable {

    private final String title;
    private final String description;
    private final URL url;

    private final int maxPlayers;
    private final int minPlayers;

    public static Game of(String title, int numberOfPlayers) {
        return new Game(title, numberOfPlayers, numberOfPlayers, null, null);
    }

    public static Game of(String title, int numberOfPlayers, String description) {
        return new Game(title, numberOfPlayers, numberOfPlayers, description, null);
    }

    public static Game of(String title, int numberOfPlayers, String description, URL url) {
        return new Game(title, numberOfPlayers, numberOfPlayers, description, url);
    }

    public static Game of(String title, int minPlayers, int maxPlayers) {
        return new Game(title, minPlayers, maxPlayers, null, null);
    }

    public static Game of(String title, int minPlayers, int maxPlayers, String description) {
        return new Game(title, minPlayers, maxPlayers, description, null);
    }

    public static Game of(String title, int minPlayers, int maxPlayers, String description, URL url) {
        return new Game(title, minPlayers, maxPlayers, description, url);
    }

    private Game(String title, int minPlayers, int maxPlayers, String description, URL url) {
        Preconditions.checkArgument(minPlayers > 1, "There must be at least one player in each game!");
        Preconditions.checkArgument(minPlayers <= maxPlayers, "Minimum number of players must be greater or equal to maximum number of players!");

        this.title = Preconditions.checkNotNull(title, "Title cannot be null!");

        this.description = description;
        this.url = url;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<URL> getURL() {
        return Optional.ofNullable(url);
    }


    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return Objects.equal(title, game.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title);
    }

    @Override
    public String toString() {
        return "Game '" + title + "'";
    }

    @Override
    public Map<String, Object> toMap() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("title", title);
        getDescription().ifPresent(s -> builder.put("description", s));
        getURL().ifPresent(s -> builder.put("url", s.toExternalForm()));
        builder.put("maxPlayers", maxPlayers);
        builder.put("minPlayers", minPlayers);
        return builder.build();
    }

    static Game load(Object game) {
        if (game == null) {
            return null;
        }
        if (!(game instanceof Map)) {
            throw new IllegalArgumentException("Cannot load game from " + game);
        }
        Map<String, Object> map = (Map<String, Object>) game;

        return new Game(
            RemoteSession.loadString(map, "title"),
            RemoteSession.loadInteger(map, "minPlayers"),
            RemoteSession.loadInteger(map, "maxPlayers"),
            RemoteSession.loadString(map, "description"),
            RemoteSession.loadURL(map, "url")
        );
    }
}
