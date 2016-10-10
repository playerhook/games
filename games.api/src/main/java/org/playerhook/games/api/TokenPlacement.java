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


    public static TokenPlacement create(Token token, Player player, Position destination) {
        return new TokenPlacement(token, player, null, destination, null);
    }

    public static TokenPlacement create(Token token, Player player, Position destination, String key) {
        return new TokenPlacement(token, player, null, destination, key);
    }

    public TokenPlacement sign(String key){
        if (key.equals(this.key)) {
            return this;
        }
        return new TokenPlacement(token, player, source, destination, key);
    }

    public TokenPlacement unsigned(){
        if (key == null) {
            return this;
        }
        return new TokenPlacement(token, player, source, destination, null);
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

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    public String toString() {
        if (source == null) {
            return getPlayer().toString() + " placed " + getToken() + " on " + getDestination();
        }
        return getPlayer().toString() + " moved " + getToken() + " from " + source + " to " + getDestination();
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

    public static TokenPlacement load(Object tokenPlacement) {
        if (tokenPlacement == null) {
            return null;
        }
        if (!(tokenPlacement instanceof Map)) {
            throw new IllegalArgumentException("Cannot load token placement from " + tokenPlacement);
        }
        Map<String, Object> map = (Map<String, Object>) tokenPlacement;

        return new TokenPlacement(
            new Token.Stub(MapSerializable.loadString(map, "token")),
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
        return Objects.equal(token, placement.token) &&
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
