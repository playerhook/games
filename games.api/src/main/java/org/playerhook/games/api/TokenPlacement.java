package org.playerhook.games.api;

import java.util.Optional;

public final class TokenPlacement {

    private final Token token;
    private final Player player;
    private final Position source;
    private final Position destination;

    public static TokenPlacement of(Token token, Player player, Position destination) {
        return new TokenPlacement(token, player, null, destination);
    }

    private TokenPlacement(Token token, Player player, Position source, Position destination) {
        this.token = token;
        this.player = player;
        this.destination = destination;
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
}
