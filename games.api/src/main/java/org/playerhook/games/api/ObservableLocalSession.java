package org.playerhook.games.api;

import rx.Observable;

public interface ObservableLocalSession extends LocalSession {

    Observable<SessionUpdate> observe();

}
