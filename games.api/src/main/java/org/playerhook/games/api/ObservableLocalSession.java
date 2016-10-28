package org.playerhook.games.api;

import rx.Observable;

/**
 * @deprecated since sessions are now immutable the changes are more obvious from the code an should be handled manually
 */
@Deprecated
public interface ObservableLocalSession extends LocalSession {

    Observable<SessionUpdate> observe();

}
