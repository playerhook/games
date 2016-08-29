package org.playerhook.games.api;

public class SessionUpdate {

    private final Session session;
    private final SessionUpdateType type;

    public static SessionUpdate of(Session session, SessionUpdateType type) {
        return new SessionUpdate(session, type);
    }

    private SessionUpdate(Session session, SessionUpdateType type) {
        this.session = session;
        this.type = type;
    }

    public Session getSession() {
        return session;
    }

    public SessionUpdateType getType() {
        return type;
    }
}
