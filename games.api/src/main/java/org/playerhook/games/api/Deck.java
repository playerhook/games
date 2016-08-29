package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public final class Deck {

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

    private Deck(ImmutableList<Token> tokens, int totalTokensAvailable) {
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
}
