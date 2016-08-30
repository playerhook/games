package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public final class Move implements MapSerializable {

    private final TokenPlacement tokenPlacement;
    private final RuleViolation ruleViolation;
    private final Instant timestamp;

    public static Move to(TokenPlacement tokenPlacement) {
        return to(tokenPlacement, null, Instant.now());
    }

    public static Move to(TokenPlacement tokenPlacement, Instant timestamp) {
        return to(tokenPlacement, null, timestamp);
    }

    public static Move to(TokenPlacement tokenPlacement, RuleViolation ruleViolation) {
        return to(tokenPlacement, ruleViolation, Instant.now());
    }

    public static Move to(TokenPlacement tokenPlacement, RuleViolation ruleViolation, Instant timestamp) {
        return new Move(tokenPlacement, ruleViolation, timestamp);
    }

    private Move(TokenPlacement tokenPlacement, RuleViolation ruleViolation, Instant timestamp) {
        this.tokenPlacement = Preconditions.checkNotNull(tokenPlacement, "Token placement cannot be null!");
        this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp cannot be null!");
        this.ruleViolation = ruleViolation;
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

    @Override
    public Map<String, Object> toMap() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("tokenPlacement", tokenPlacement.toMap());
        builder.put("timestamp", timestamp.toEpochMilli());

        getRuleViolation().ifPresent(ruleViolation -> builder.put("ruleViolation", ruleViolation.getCode()));

        return builder.build();
    }

    static Move load(Object move) {
        if (move == null) {
            return null;
        }
        if (!(move instanceof Map)) {
            throw new IllegalArgumentException("Cannot load move from " + move);
        }
        Map<String, Object> map = (Map<String, Object>) move;

        return new Move(
            TokenPlacement.load(map.get("tokenPlacement")),
            RuleViolation.load(RemoteSession.loadString(map, "ruleViolation")),
            RemoteSession.loadInstant(map, "timestamp")
        );
    }
}
