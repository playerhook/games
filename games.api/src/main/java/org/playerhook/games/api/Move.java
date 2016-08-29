package org.playerhook.games.api;

import com.google.common.base.Objects;

import java.time.Instant;
import java.util.Optional;

public final class Move {

    private final TokenPlacement tokenPlacement;
    private final RuleViolation ruleViolation;
    private final Instant timestamp;

    public static Move of(TokenPlacement tokenPlacement) {
        return of(tokenPlacement, null, Instant.now());
    }

    public static Move of(TokenPlacement tokenPlacement, Instant timestamp) {
        return of(tokenPlacement, null, timestamp);
    }

    public static Move of(TokenPlacement tokenPlacement, RuleViolation ruleViolation) {
        return of(tokenPlacement, ruleViolation, Instant.now());
    }

    public static Move of(TokenPlacement tokenPlacement, RuleViolation ruleViolation, Instant timestamp) {
        return new Move(tokenPlacement, ruleViolation, timestamp);
    }

    private Move(TokenPlacement tokenPlacement, RuleViolation ruleViolation, Instant timestamp) {
        this.tokenPlacement = tokenPlacement;
        this.ruleViolation = ruleViolation;
        this.timestamp = timestamp;
    }

    public TokenPlacement getTokenPlacement() {
        return tokenPlacement;
    }

    public Optional<RuleViolation> getRuleViolation() {
        return Optional.ofNullable(ruleViolation);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Move move = (Move) o;
        return Objects.equal(tokenPlacement, move.tokenPlacement) && Objects.equal(ruleViolation, move.ruleViolation) && Objects.equal(timestamp, move.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokenPlacement, ruleViolation, timestamp);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(tokenPlacement.toString());
        builder.append(" on ").append(timestamp);
        if (ruleViolation != null) {
            builder.append(" causing ").append(ruleViolation.getMessage());
        }
        return builder.toString();
    }
}
