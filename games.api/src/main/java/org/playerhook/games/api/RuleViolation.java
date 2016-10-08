package org.playerhook.games.api;

public interface RuleViolation {

    String getCode();
    String getMessage();

    enum Default implements RuleViolation {
        NOT_YOUR_TURN("This is not your turn!"),
        POSITION_ALREADY_TAKEN("The position given is already taken!"),
        TOKEN_NOT_ALLOWED_ON_GIVEN_POSITION("You cannot place your token at given position!"),
        ILLEGAL_TOKEN("You cannot play with this token!"),
        GAME_NOT_STARTED_YET("Game has't started yet!"),
        KEY_MISMATCH("Session key does not match"),
        KEY_MISSING("Session is signed but no player key is present");

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

    class Stub implements RuleViolation {
        private final String code;

        Stub(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return code;
        }

        @Override
        public String toString() {
            return getMessage();
        }
    }

    public static RuleViolation load(String code) {
        if (code == null) {
            return null;
        }
        for (Default d : Default.values()) {
            if (d.getCode().equals(code)) {
                return d;
            }
        }
        return new Stub(code);
    }

    static boolean equals(RuleViolation token, RuleViolation another) {
        return token.getCode().equals(another.getCode());
    }

}
