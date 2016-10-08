package org.playerhook.games.api;

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

    public static TokenPlacement create(Token token, Player player, Position destination) {
        return new TokenPlacement(token, player, null, destination);
    }

    private TokenPlacement(Token token, Player player, Position source, Position destination) {
        this.token = Preconditions.checkNotNull(token, "Token cannot be null");
        this.player = Preconditions.checkNotNull(player, "Player cannot be null");
        this.destination = Preconditions.checkNotNull(destination, "Destination cannot be null");
        this.source = source;
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
            Position.load(map.get("destination"))
        );
    }
}
