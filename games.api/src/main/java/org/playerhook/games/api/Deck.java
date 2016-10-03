package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.Map;
import java.util.stream.Collectors;

public final class Deck implements MapSerializable {

    private final ImmutableList<Token> tokens;
    private final int totalTokensAvailable;

    public static Deck of(Iterable<Token> tokens) {
        return new Deck(ImmutableList.copyOf(tokens), Integer.MAX_VALUE);
    }

    public static Deck of(Iterable<Token> tokens, int totalTokensAvailable) {
        return new Deck(ImmutableList.copyOf(tokens), totalTokensAvailable);
    }

    public static Deck of(Token... tokens) {
        return new Deck(ImmutableList.copyOf(tokens), Integer.MAX_VALUE);
    }

    public static Deck ofSame(Token token, int total) {
        ImmutableList.Builder<Token> tokens = ImmutableList.builder();
        for (int i = 0; i < total; i++) {
            tokens.add(token);
        }
        return new Deck(tokens.build(), Integer.MAX_VALUE);
    }

    private Deck(ImmutableList<Token> tokens, int totalTokensAvailable) {
        Preconditions.checkArgument(totalTokensAvailable >= 0, "Total tokens available must be positive number");
        this.tokens = tokens;
        this.totalTokensAvailable = totalTokensAvailable;
    }

    public ImmutableList<Token> getPlayableTokens() {
        return tokens;
    }

    public int getTotalTokensAvailable() {
        return totalTokensAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Deck localDeck = (Deck) o;
        return totalTokensAvailable == localDeck.totalTokensAvailable && Objects.equal(tokens, localDeck.tokens);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokens, totalTokensAvailable);
    }

    @Override
    public Map<String, Object> toMap() {
        return ImmutableMap.of("tokens", tokens.stream().map(Token::getSymbol).collect(Collectors.toList()), "totalTokensAvailable", totalTokensAvailable);
    }

    static Deck load(Object deck) {
        if (deck == null) {
            return null;
        }
        if (!(deck instanceof Map)) {
            throw new IllegalArgumentException("Cannot load deck from " + deck);
        }
        Map<String, Object> map = (Map<String, Object>) deck;

        return new Deck(
            RemoteSession.loadList(map.getOrDefault("tokens", ImmutableList.of()), o -> new Token.Stub(o.toString())),
            RemoteSession.loadInteger(map, "totalTokensAvailable")
        );
    }
}
