package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.Map;
import java.util.Optional;

public final class TokenPlacement implements MapSerializable {

    private final Token token;
    private final Player player;
    private final Position source;
    private final Position destination;
    private final String key;


    static TokenPlacement create(Token token, Player player, Position destination) {
        return new TokenPlacement(token, player, null, destination, null);
    }

    static TokenPlacement create(Token token, Player player, Position destination, String key) {
        return new TokenPlacement(token, player, null, destination, key);
    }

    static TokenPlacement create(Token token, Player player, Position source, Position destination) {
        return new TokenPlacement(token, player, source, destination, null);
    }

    static TokenPlacement create(Token token, Player player, Position source, Position destination, String key) {
        return new TokenPlacement(token, player, source, destination, key);
    }

    public TokenPlacement sign(String key) {
        if (key == null) {
            return this;
        }
        if (key.equals(this.key)) {
            return this;
        }
        return new TokenPlacement(token, player, source, destination, key);
    }

    private TokenPlacement(Token token, Player player, Position source, Position destination, String key) {
        this.token = Preconditions.checkNotNull(token, "Token cannot be null");
        this.player = Preconditions.checkNotNull(player, "Player cannot be null");
        this.destination = Preconditions.checkNotNull(destination, "Destination cannot be null");
        this.source = source;
        this.key = key;
    }

    public Token getToken() {
        return token;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getDestination() {
        return destination;
    }

    public Optional<Position> getSource() {
        return Optional.ofNullable(source);
    }

    Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    public String toString() {
        if (source == null) {
            return player.toString() + " placed " + token + " on " + destination;
        }
        return player.toString() + " moved " + token + " from " + source + " to " + destination;
    }

    @Override
    public Map<String, Object> toMap(PrivacyLevel level) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("token", token.getSymbol());
        builder.put("player", player.toMap(level));
        getSource().ifPresent(source ->  builder.put("source", source.toMap(level)));
        getKey().ifPresent(key ->  builder.put("key", key));
        builder.put("destination", destination.toMap(level));

        return builder.build();
    }

    /**
     * Deserializes the token placement.
     *
     * Token is always a {@link org.playerhook.games.api.Token.Stub}.
     *
     * @param tokenPlacement token placement as map
     * @return token placement from map
     */
    public static TokenPlacement load(Object tokenPlacement) {
        if (tokenPlacement == null) {
            return null;
        }
        if (!(tokenPlacement instanceof Map)) {
            throw new IllegalArgumentException("Cannot load token placement from " + tokenPlacement);
        }
        Map<String, Object> map = (Map<String, Object>) tokenPlacement;

        return new TokenPlacement(
            Token.stub(MapSerializable.loadString(map, "token")),
            Player.load(map.get("player")),
            Position.load(map.get("source")),
            Position.load(map.get("destination")),
            MapSerializable.loadString(map, "key")
        );
    }

    //CHECKSTYLE:OFF

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPlacement placement = (TokenPlacement) o;
        return Token.equals(token, placement.token) &&
                Objects.equal(player, placement.player) &&
                Objects.equal(source, placement.source) &&
                Objects.equal(destination, placement.destination) &&
                Objects.equal(key, placement.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token, player, source, destination, key);
    }

    //CHECKSTYLE:ON
}
