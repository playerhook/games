package org.playerhook.games.api;

import com.google.common.base.Objects;

import java.net.URL;
import java.util.Optional;

public final class Player {

    private final String username;
    private final URL avatar;
    private final String displayName;
    private final String displayColor;

    public static Player of(String username, URL avatar, String displayName, String displayColor) {
        return new Player(username, avatar, displayName, displayColor);
    }

    public static Player of(String username, URL avatar, String displayName) {
        return of(username, avatar, displayName, null);
    }

    public static Player of(String username, URL avatar) {
        return of(username, avatar, null);
    }

    public static Player of(String username) {
        return of(username, null);
    }

    private Player(String username, URL avatar, String displayName, String displayColor) {
        this.username = username;
        this.avatar = avatar;
        this.displayName = displayName;
        this.displayColor = displayColor;
    }

    public String getUsername() {
        return username;
    }

    public Optional<URL> getAvatar() {
        return Optional.ofNullable(avatar);
    }

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public Optional<String> getDisplayColor() {
        return Optional.ofNullable(displayColor);
    }

    @Override
    public String toString() {
        return "Player '" + getDisplayName().orElse(getUsername()) + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player that = (Player) o;
        return Objects.equal(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
