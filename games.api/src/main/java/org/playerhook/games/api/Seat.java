package org.playerhook.games.api;

import rx.Observer;

public interface Seat extends Observer<Session> {

    Player getPlayer();

}
