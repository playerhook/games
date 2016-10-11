package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;

/**
 * Game token such as pawn in chess.
 *
 * The specific implementation should be an enum.
 */
public interface Token {

    Token HIDDEN = new Stub("?");

    /**
     * @return unique symbol for current game
     */
    String getSymbol();

    class Stub implements Token {

        private final String symbol;

        private Stub(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Stub stub = (Stub) o;
            return Objects.equal(symbol, stub.symbol);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(symbol);
        }

        @Override
        public String toString() {
            return getSymbol();
        }

        private static final Cache<String, Token> TOKENS = CacheBuilder.newBuilder().build();

        static Token get(String symbol) {
            Token token = TOKENS.getIfPresent(symbol);
            if (token != null) {
                return token;
            }
            token = new Stub(symbol);
            TOKENS.put(symbol, token);
            return token;
        }
    }

    static boolean equals(Token token, Token another) {
        return token.getSymbol().equals(another.getSymbol());
    }

    static boolean contains(Iterable<Token> tokens, Token another) {
        return Iterables.contains(Iterables.transform(tokens, Token::getSymbol), another.getSymbol());
    }

    static Token stub(String symbol) {
        return Stub.get(symbol);
    }

}
