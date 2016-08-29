package org.playerhook.games.api;

public final class TokenPlacement {

    private final Token token;
    private final Player player;
    private final Position position;

    public static TokenPlacement of(Token token, Player player, Position position) {
        return new TokenPlacement(token, player, position);
    }

    private TokenPlacement(Token token, Player player, Position position) {
        this.token = token;
        this.player = player;
        this.position = position;
    }

    public Token getToken() {
        return token;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getPosition() {
        return position;
    }

    public String toString() {
        return getPlayer().toString() + " placed " + getToken() + " on " + getPosition();
    }
}
