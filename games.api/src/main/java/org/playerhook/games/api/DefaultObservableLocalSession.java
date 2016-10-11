package org.playerhook.games.api;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

final class DefaultObservableLocalSession implements ObservableLocalSession {

    private final LocalSession delegate;
    private final PublishSubject<SessionUpdate> subject;

    private DefaultObservableLocalSession(LocalSession delegate, PublishSubject<SessionUpdate> subject) {
        this.delegate = delegate;
        this.subject = subject;
    }

    DefaultObservableLocalSession(LocalSession delegate) {
        this(delegate, PublishSubject.create());
    }

    @Override
    public Observable<SessionUpdate> observe() {
        return subject;
    }

    @Override
    public Game getGame() {
        return delegate.getGame();
    }

    @Override
    public Board getBoard() {
        return delegate.getBoard();
    }

    @Override
    public ImmutableList<Player> getPlayers() {
        return delegate.getPlayers();
    }

    @Override
    public Optional<Player> getPlayerOnTurn() {
        return delegate.getPlayerOnTurn();
    }

    @Override
    public ImmutableList<Move> getMoves() {
        return delegate.getMoves();
    }

    @Override
    public Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public Optional<URL> getURL() {
        return delegate.getURL();
    }

    @Override
    public ObservableLocalSession join(Player newPlayer) {
        return safeWithUpdate(() -> delegate.join(newPlayer));
    }

    @Override
    public ObservableLocalSession play(TokenPlacement placement) {
        return safeWithUpdate(() -> delegate.play(placement));
    }

    @Override
    public ObservableLocalSession signWith(String privateKey) {
        return safeWithUpdate(() -> delegate.signWith(privateKey));
    }

    @Override
    public Deck getDeck(Player player) {
        return delegate.getDeck(player);
    }

    @Override
    public Optional<String> getKey(Player player) {
        return delegate.getKey(player);
    }

    @Override
    public int getScore(Player player) {
        return delegate.getScore(player);
    }

    @Override
    public TokenPlacement sign(TokenPlacement placement) {
        return delegate.sign(placement);
    }

    @Override
    public ObservableLocalSession start() {
        return safeWithUpdate(delegate::start);
    }

    @Override
    public ObservableLocalSession suspend() {
        return safeWithUpdate(delegate::suspend);
    }

    private void sendUpdate(LocalSession original, LocalSession updated) {
        SessionUpdate.diff(original, updated).ifPresent(update -> {
            subject.onNext(update);
            if (update.getType() == SessionUpdateType.Default.STATUS && updated.getStatus() == Status.FINISHED) {
                subject.onCompleted();
            }
        });
    }

    @Override
    public boolean hasEmptySeat() {
        return delegate.hasEmptySeat();
    }

    @Override
    public boolean canStart() {
        return delegate.canStart();
    }

    @Override
    public Map<String, Object> toMap(PrivacyLevel level) {
        return delegate.toMap(level);
    }

    private ObservableLocalSession safeWithUpdate(UnsafeSessionOperation op) {
        try {
            LocalSession session = op.run();
            sendUpdate(delegate, session);
            return new DefaultObservableLocalSession(session, subject);
        } catch (Exception e) {
            subject.onError(e);
            throw e;
        }
    }

    @FunctionalInterface private interface UnsafeSessionOperation {
        LocalSession run();
    }

    @Override
    public TokenPlacement newPlacement(Token token, Player player, Position source, Position destination) {
        return delegate.newPlacement(token, player, source, destination);
    }

    //CHECKSTYLE:OFF
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultObservableLocalSession that = (DefaultObservableLocalSession) o;
        return Objects.equal(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }
    //CHECKSTYLE:OF
}
