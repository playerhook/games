package org.playerhook.games.api;

import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public interface Rules extends MapSerializable {

    static Rules stub(String description, String type, int minPlayers, int maxPlayers) {
        return new Stub(description, type, minPlayers, maxPlayers);
    }

    class Stub implements Rules {
        private final String description;
        private final String type;
        private final int minPlayers;
        private final int maxPlayers;

        Stub(String description, String type, int minPlayers, int maxPlayers) {
            this.description = description;
            this.type = type;
            this.minPlayers = minPlayers;
            this.maxPlayers = maxPlayers;
        }

        @Override
        public Deck prepareDeck(Session session, Player player) {
            throw new UnsupportedOperationException("Rule implemenation is missing on the classpath");
        }

        @Override
        public EvaluationResult evaluate(Session session, TokenPlacement placement) {
            throw new UnsupportedOperationException("Rule implemenation is missing on the classpath");
        }

        @Override
        public int getMinPlayers() {
            return minPlayers;
        }

        @Override
        public int getMaxPlayers() {
            return maxPlayers;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getType() {
            return type;
        }
    }
    class EvaluationResult {

        public static Builder builder(TokenPlacement placement) {
            return new Rules.EvaluationResult.Builder(placement);
        }

        private final Move move;
        private final ImmutableMap<Player, Integer> scoreUpdates;
        private final Status nextStatus;
        private final Player nextPlayer;

        EvaluationResult(Move move, ImmutableMap<Player, Integer> scoreUpdates, Status nextStatus, Player nextPlayer) {
            this.move = checkNotNull(move, "Move cannot must be set");
            this.nextStatus = nextStatus;
            this.nextPlayer = checkNotNull(nextPlayer, "Next player must be set (even the game has been finished already)");
            this.scoreUpdates = Optional.ofNullable(scoreUpdates).orElse(ImmutableMap.of());
        }

        public Move getMove() {
            return move;
        }

        public ImmutableMap<Player, Integer> getScoreUpdates() {
            return scoreUpdates;
        }

        public Optional<Status> getNextStatus() {
            return Optional.ofNullable(nextStatus);
        }

        public Player getNextPlayer() {
            return nextPlayer;
        }

        public static class Builder {

            private final TokenPlacement placement;

            private RuleViolation violation;
            private ImmutableMap.Builder<Player, Integer> scoreChanges = ImmutableMap.builder();
            private Status nextStatus;
            private Player nextPlayer;

            private Builder(TokenPlacement placement) {
                this.placement = placement;
            }

            public Builder updateScore(Player player, int update) {
                this.scoreChanges.put(player, update);
                return this;
            }

            public Builder nextPlayer(Player player) {
                this.nextPlayer = player;
                return this;
            }

            public Builder nextStatus(Status status) {
                this.nextStatus = status;
                return this;
            }

            public Builder finishGame() {
                this.nextStatus = Status.FINISHED;
                return this;
            }

            public Builder ruleViolation(RuleViolation violation) {
                this.violation = violation;
                return this;
            }

            public EvaluationResult build() {
                return new EvaluationResult(
                        Move.to(placement, violation),
                        scoreChanges.build(),
                        nextStatus,
                        Optional.ofNullable(nextPlayer).orElse(placement.getPlayer())
                );
            }
        }

    }

    Deck prepareDeck(Session session, Player player);
    EvaluationResult evaluate(Session session, TokenPlacement placement);
    int getMinPlayers();
    int getMaxPlayers();
    String getDescription();

    default String getType() {
        return getClass().getName();
    }

    @Override
    default Map<String, Object> toMap(boolean includeInternalState) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("description", getDescription());
        builder.put("minPlayers", getMinPlayers());
        builder.put("maxPlayers", getMaxPlayers());
        builder.put("type", getType());

        return builder.build();
    }

    static Rules load(Object rules) {
        if (rules == null) {
            return null;
        }
        if (!(rules instanceof Map)) {
            throw new IllegalArgumentException("Cannot load rules from " + rules);
        }
        Map<String, Object> map = (Map<String, Object>) rules;

        String ruleClassName = Optional
                .ofNullable(map.get("type"))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("rule is missing!"));

        try {
            Class<?> clazz = Class.forName(ruleClassName);
            if (Rules.class.isAssignableFrom(clazz)) {
                return (Rules) clazz.newInstance();
            }
            throw new IllegalArgumentException(ruleClassName + " is not a rule");
        } catch (Exception e) {
            return Rules.stub(
                MapSerializable.loadString(map, "description"),
                MapSerializable.loadString(map, "type"),
                MapSerializable.loadInteger(map, "minPlayers"),
                MapSerializable.loadInteger(map, "maxPlayers")
            );
        }

    }
}
