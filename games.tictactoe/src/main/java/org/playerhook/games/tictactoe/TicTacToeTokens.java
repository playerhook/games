package org.playerhook.games.tictactoe;

import org.playerhook.games.api.Token;

public enum TicTacToeTokens implements Token {
    CIRCLE("o"),
    CROSS("x");

    private final String symbol;

    TicTacToeTokens(String symbol) {
        this.symbol = symbol;
    }


    @Override
    public String getSymbol() {
        return symbol;
    }
}
