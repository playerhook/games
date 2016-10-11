package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.util.Map;
import java.util.Optional;

public final class SessionUpdate implements MapSerializable {

    private final Session session;
    private final SessionUpdateType type;

    public static SessionUpdate of(Session session, SessionUpdateType type) {
        return new SessionUpdate(session, type);
    }

    public static Optional<SessionUpdate> diff(Session original, Session updated) {
        if (!original.getStatus().equals(updated.getStatus())) {
            return Optional.of(SessionUpdate.of(updated, SessionUpdateType.Default.STATUS));
        }
        if (!original.getPlayers().equals(updated.getPlayers())) {
            return Optional.of(SessionUpdate.of(updated, SessionUpdateType.Default.PLAYER));
        }
        if (!original.getMoves().equals(updated.getMoves())) {
            return Optional.of(SessionUpdate.of(updated, SessionUpdateType.Default.MOVE));
        }
        return Optional.empty();
    }

    private SessionUpdate(Session session, SessionUpdateType type) {
        this.session = Preconditions.checkNotNull(session, "Session cannot be null");
        this.type = Preconditions.checkNotNull(type, "Type cannot be null");
    }

    public Session getSession() {
        return session;
    }

    public SessionUpdateType getType() {
        return type;
    }

    @Override
    public Map<String, Object> toMap(PrivacyLevel level) {
        return ImmutableMap.of("session", session.toMap(level), "type", type.getCode());
    }

    public static SessionUpdate materialize(Object payload) {
        if (payload == null) {
            return null;
        }
        if (!(payload instanceof Map)) {
            throw new IllegalArgumentException("Cannot load session update from " + payload);
        }
        Map<String, Object> map = (Map<String, Object>) payload;
        return new SessionUpdate(DefaultSession.load(map.get("session")),
            SessionUpdateType.load(MapSerializable.loadString(map, "type"))
        );
    }

    @Override
    public String toString() {
        return "Update: " + type + " for " + session;
    }

    //CHECKSTYLE:OFF

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionUpdate update = (SessionUpdate) o;
        return Objects.equal(session, update.session) &&
                Objects.equal(type, update.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(session, type);
    }

    //CHECKSTYLE:ON
}
