package org.playerhook.games.api;

import com.google.common.base.Objects;

public interface SessionUpdateType {

    String getCode();

    public static SessionUpdateType load(String code) {
        if (code == null) {
            return null;
        }
        for (SessionUpdateType.Default d : SessionUpdateType.Default.values()) {
            if (d.getCode().equals(code)) {
                return d;
            }
        }
        return new Stub(code);
    }

    enum Default implements SessionUpdateType {
        /**
         * New move was placed.
         */
        MOVE,

        /**
         * Player has joined or leaved the game.
         */
        PLAYER,

        /**
         * Session is ready to accept next moves.
         */
        READY,

        /**
         * Status of the game has changed.
         *
         * For example the game has started
         */
        STATUS;


        @Override
        public String getCode() {
            return name();
        }
    }

    class Stub implements SessionUpdateType {
        private final String code;

        public Stub(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
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
            return Objects.equal(code, stub.code);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(code);
        }

        @Override
        public String toString() {
            return getCode();
        }
    }

}
