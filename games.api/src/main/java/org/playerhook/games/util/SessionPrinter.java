package org.playerhook.games.util;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.playerhook.games.api.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public final class SessionPrinter {

    private final PrintWriter out;

    public static SessionPrinter writeTo(PrintWriter writer) {
        return new SessionPrinter(writer);
    }

    public static SessionPrinter out() {
        return new SessionPrinter();
    }

    public static String toString(SessionUpdate update) {
        StringWriter sw = new StringWriter();
        new SessionPrinter(new PrintWriter(sw)).print(update);
        return sw.toString();
    }

    public static String toString(Session session) {
        StringWriter sw = new StringWriter();
        new SessionPrinter(new PrintWriter(sw)).print(session);
        return sw.toString();
    }

    public SessionPrinter(PrintWriter out) {
        this.out = out;
    }

    public SessionPrinter() {
        this(new PrintWriter(System.out));
    }
    
    public void print(SessionUpdate update) {
        print(update.getSession());
        out.println("Updated: " + update.getType().toString());
    }

    public void print(Session session) {
        out.println(Strings.repeat("=", 60));
        out.println(center(session.getGame().getTitle(), 60, ' '));
        out.println(Strings.repeat("-", 60));
        session.getURL().ifPresent(url -> {
            out.println(center(url.toString(), 60, ' '));
            out.println(Strings.repeat("-", 60));
        });

        if (!session.getMoves().isEmpty()) {
            Move last = Iterables.getLast(session.getMoves());
            if (last.getRuleViolation().isPresent()) {
                out.println(Strings.repeat("-", 60));
                out.println(last.getRuleViolation().get().getMessage());
            }
        }

        int boardSectionWidth = session.getBoard().getWidth() * 2 + 1;

        for (Player player: session.getPlayers()) {
            if (player.equals(session.getPlayerOnTurn().orElse(null))) {
                out.print("* ");
            } else {
                out.print("  ");
            }
            out.print(Strings.padEnd(player.getUsername(), 40, ' '));
            out.print(": ");
            out.print(Strings.padStart(String.valueOf(session.getScore(player)), 5, '0'));
            out.println();
        }

        out.println(Strings.repeat("=", 60));
        out.println(Strings.repeat("-", boardSectionWidth));

        for (int row = session.getBoard().getFirstRow(); row <= session.getBoard().getLastRow(); row++) {
            out.print("|");
            for (int col = session.getBoard().getFirstColumn(); col <= session.getBoard().getLastColumn(); col++) {
                Optional<TokenPlacement> optional = session.getBoard().getTokenPlacement(Position.at(row, col));
                if (optional.isPresent()) {
                    TokenPlacement tokenPlacement = optional.get();
                    String symbol = tokenPlacement.getToken().getSymbol();
                    if (isLastPlacement(session, tokenPlacement)) {
                        symbol = symbol.toUpperCase();
                    }
                    out.print(symbol);
                } else {
                    out.print(" ");
                }
                out.print("|");
            }
            out.println();
            out.println(Strings.repeat("-", boardSectionWidth));
        }
        out.println(Strings.repeat("-", 60));
        out.flush();
    }

    private boolean isLastPlacement(Session session, TokenPlacement tokenPlacement) {
        if (session.getMoves().isEmpty()) {
            return false;
        }
        return Iterables.getLast(session.getMoves()).getTokenPlacement().equals(tokenPlacement);
    }

    private static String center(String s, int size, char pad) {
        if (s == null || size <= s.length()) {
            return s;
        }

        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < (size - s.length()) / 2; i++) {
            sb.append(pad);
        }
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }
}
