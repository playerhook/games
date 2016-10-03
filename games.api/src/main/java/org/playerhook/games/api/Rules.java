package org.playerhook.games.api;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public interface Rules {

    Rules NOT_FOUND = new Rules() {
        @Override
        public Deck prepareDeck(Session session, Player player) {
            return Deck.of();
        }

        @Override
        public EvaluationResult evaluate(Session session, TokenPlacement placement) {
            return EvaluationResult.builder(placement).build();
        }

        @Override
        public int getMinPlayers() {
            return -1;
        }

        @Override
        public int getMaxPlayers() {
            return -1;
        }

        @Override
        public String getDescription() {
            return "Rules not found!";
        }
    };

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

}
