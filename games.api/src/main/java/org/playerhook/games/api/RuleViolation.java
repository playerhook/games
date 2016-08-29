package org.playerhook.games.api;

public interface RuleViolation {

    String getCode();
    String getMessage();

    enum Default implements RuleViolation {
        NOT_YOUR_TURN("This is not your turn!"),
        POSITION_ALREADY_TAKEN("The position given is already taken!"),
        TOKEN_NOT_ALLOWED_ON_GIVEN_POSITION("You cannot place your token at given position!"),
        ILLEGAL_TOKEN("You cannot play with this token!"),
        GAME_NOT_STARTED_YET("Game has't started yet!");

        private final String message;

        Default(String message) {
            this.message = message;
        }

        @Override
        public String getCode() {
            return name();
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

}
