package org.playerhook.games.api;

import com.google.common.base.Objects;

import java.net.URL;
import java.util.Optional;

public final class Game {

    private final String title;
    private final String description;
    private final URL url;

    public static Game of(String title) {
        return new Game(title, null, null);
    }

    public static Game of(String title, String description) {
        return new Game(title, description, null);
    }

    public static Game of(String title, String description, URL url) {
        return new Game(title, description, url);
    }

    private Game(String title, String description, URL url) {
        this.title = title;
        this.description = description;
        this.url = url;
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
}
