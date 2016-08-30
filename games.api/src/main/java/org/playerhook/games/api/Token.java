package org.playerhook.games.api;

import com.google.common.base.Objects;

/**
 * Game token such as pawn in chess.
 *
 * The specific implementation should be an enum.
 */
public interface Token {

    /**
     * @return unique symbol for current game
     */
    String getSymbol();

    class Stub implements Token {

        private final String symbol;

        public Stub(String symbol) {
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
    }

    static boolean equals(Token token, Token another) {
        return token.getSymbol().equals(another.getSymbol());
    }

}
