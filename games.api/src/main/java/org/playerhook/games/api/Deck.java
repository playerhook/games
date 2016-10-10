package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Deck implements MapSerializable {

    private final ImmutableList<Token> tokens;
    private final ImmutableList<Token> secretTokens;

    public static Deck of(Iterable<Token> tokens) {
        return new Deck(ImmutableList.copyOf(tokens), ImmutableList.of());
    }

    public static Deck of(Iterable<Token> tokens, Iterable<Token> secretTokens) {
        return new Deck(ImmutableList.copyOf(tokens), ImmutableList.copyOf(secretTokens));
    }

    public static Deck of(Token... tokens) {
        return new Deck(ImmutableList.copyOf(tokens), ImmutableList.of());
    }

    public static Deck ofSame(Token token, int total) {
        ImmutableList.Builder<Token> tokens = ImmutableList.builder();
        for (int i = 0; i < total; i++) {
            tokens.add(token);
        }
        return new Deck(tokens.build(), ImmutableList.of());
    }

    private Deck(ImmutableList<Token> tokens, ImmutableList<Token> secretTokens) {
        Preconditions.checkArgument((tokens.size() + secretTokens.size()) >= 0, "Total tokens available must be positive number");
        this.tokens = tokens;
        this.secretTokens = secretTokens;
    }

    public ImmutableList<Token> getPlayableTokens() {
        return tokens;
    }

    public ImmutableList<Token> getSecretTokens() {
        return secretTokens;
    }

    @Override
    public Map<String, Object> toMap(PrivacyLevel level) {
        if (PrivacyLevel.PUBLIC.equals(level)) {
            return ImmutableMap.of(
                    "tokens", tokens.stream().map(Token::getSymbol).collect(Collectors.toList()),
                    "secretTokens", tokens.stream().map(token -> "?").collect(Collectors.toList())
            );
        }
        return ImmutableMap.of(
                "tokens", tokens.stream().map(Token::getSymbol).collect(Collectors.toList()),
                "secretTokens", tokens.stream().map(Token::getSymbol).collect(Collectors.toList())
        );
    }

    public static Deck load(Object deck) {
        if (deck == null) {
            return null;
        }
        if (!(deck instanceof Map)) {
            throw new IllegalArgumentException("Cannot load deck from " + deck);
        }
        Map<String, Object> map = (Map<String, Object>) deck;


        return new Deck(
            MapSerializable.loadList(map.getOrDefault("tokens", ImmutableList.of()), o -> new Token.Stub(o.toString())),
            MapSerializable.loadList(map.getOrDefault("secretTokens", ImmutableList.of()), o -> new Token.Stub(o.toString()))
        );
    }

    public Deck remove(Token token) {
        int index = tokens.indexOf(token);

        if (index == -1) {
            return this;
        }

        List<Token> copy = new ArrayList<Token>(tokens);
        copy.remove(index);
        return of(copy, secretTokens);
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deck deck = (Deck) o;
        return Objects.equal(tokens, deck.tokens) &&
                Objects.equal(secretTokens, deck.secretTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokens, secretTokens);
    }
    //CHECKSTYLE:ON
}
