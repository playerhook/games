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
    private final Rules rules;
    private final URL url;

    public static Game of(String title, Rules rules) {
        return new Game(title, rules, null, null);
    }

    public static Game of(String title, String description, Rules rules) {
        return new Game(title, rules, description, null);
    }

    public static Game of(String title, String description, URL url, Rules rules) {
        return new Game(title, rules, description, url);
    }

    private Game(String title, Rules rules, String description, URL url) {
        this.title = Preconditions.checkNotNull(title, "Title cannot be null!");
        this.description = description;
        this.url = url;
        this.rules = Preconditions.checkNotNull(rules, "Rules cannot be null");
    }

    public String getTitle() {
        return title;
    }

    public Rules getRules() {
        return rules;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<URL> getURL() {
        return Optional.ofNullable(url);
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
        builder.put("rules", rules.getClass().getName());
        getDescription().ifPresent(s -> builder.put("description", s));
        getURL().ifPresent(s -> builder.put("url", s.toExternalForm()));
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

        Rules rules;

        try {
            rules = MapSerializable.loadRules(map, "rules");
        } catch(IllegalArgumentException e) {
            rules = Rules.NOT_FOUND;
        }

        return new Game(
                MapSerializable.loadString(map, "title"),
                rules,
                MapSerializable.loadString(map, "description"),
                MapSerializable.loadURL(map, "url")
        );
    }
}
