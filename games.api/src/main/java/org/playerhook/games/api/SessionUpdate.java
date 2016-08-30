package org.playerhook.games.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.playerhook.games.util.MapSerializable;

import java.net.URL;
import java.util.Map;
import java.util.function.BiConsumer;

public final class SessionUpdate implements MapSerializable {

    private final Session session;
    private final SessionUpdateType type;

    public static SessionUpdate of(Session session, SessionUpdateType type) {
        return new SessionUpdate(session, type);
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
    public Map<String, Object> toMap() {
        return ImmutableMap.of("session", session.toMap(), "type", type.getCode());
    }

    public static SessionUpdate materialize(Object payload, BiConsumer<URL, TokenPlacement> onPlay) {
        if (payload == null) {
            return null;
        }
        if (!(payload instanceof Map)) {
            throw new IllegalArgumentException("Cannot load session update from " + payload);
        }
        Map<String, Object> map = (Map<String, Object>) payload;
        return new SessionUpdate(
            new RemoteSession(map.get("session"), onPlay),
            SessionUpdateType.load(RemoteSession.loadString(map, "type"))
        );
    }
}
