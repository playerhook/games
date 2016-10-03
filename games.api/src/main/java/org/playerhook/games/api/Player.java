package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

public final class Player implements MapSerializable {

    private final String username;
    private final URL avatar;
    private final String displayName;
    private final String displayColor;

    public static Player create(String username, URL avatar, String displayName, String displayColor) {
        return new Player(username, avatar, displayName, displayColor);
    }

    public static Player create(String username, URL avatar, String displayName) {
        return create(username, avatar, displayName, null);
    }

    public static Player create(String username, URL avatar) {
        return create(username, avatar, null);
    }

    public static Player create(String username) {
        return create(username, null);
    }

    private Player(String username, URL avatar, String displayName, String displayColor) {
        this.username = Preconditions.checkNotNull(username, "Username cannot be null");
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

    @Override
    public Map<String, Object> toMap(boolean includeInternalState) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("username", username);
        getAvatar().ifPresent(url -> builder.put("avatar", url.toExternalForm()));
        getDisplayName().ifPresent(displayName -> builder.put("displayName", displayName));
        getDisplayColor().ifPresent(displayColor -> builder.put("displayColor", displayColor));

        return builder.build();
    }

    public static Player load(Object player) {
        if (player == null) {
            return null;
        }
        if (!(player instanceof Map)) {
            throw new IllegalArgumentException("Cannot load player from " + player);
        }
        Map<String, Object> map = (Map<String, Object>) player;

        return new Player(
            MapSerializable.loadString(map, "username"),
            MapSerializable.loadURL(map, "avatar"),
            MapSerializable.loadString(map, "displayName"),
            MapSerializable.loadString(map, "displayColor")
        );
    }
}
