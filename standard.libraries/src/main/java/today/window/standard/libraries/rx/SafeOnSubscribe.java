package today.window.standard.libraries.rx;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Simple {@link rx.Observable.OnSubscribe} stub which handles the errors properly and calls {@link Subscriber#onStart()}
 * and {@link Subscriber#onCompleted()} properly.
 */
public final class SafeOnSubscribe<T> implements Observable.OnSubscribe<T> {

    /**
     * Represents unsafe generator action which relies on the proper handling of {@link SafeOnSubscribe}.
     * @param <T> the type of items generated from the {@link Observable}
     */
    @FunctionalInterface
    interface UnsafeAction<T> {
        void call(Action1<T> t) throws Throwable;
    }

    /**
     * Represents unsafe generator action which relies on the proper handling of {@link SafeOnSubscribe}.
     *
     * This method generates function to be used with {@link Observable#flatMap(Func1)}
     *
     * @param <S> the type of items generated from the previous {@link Observable}
     * @param <T> the type of items generated from the {@link Observable}
     */
    @FunctionalInterface
    interface UnsafeActionFunction<S, T> {
        void call(S source, Action1<T> onNext) throws Throwable;
    }

    private final UnsafeAction<T> onNext;

    private SafeOnSubscribe(UnsafeAction<T> onNext) {
        this.onNext = onNext;
    }

    /**
     * Create new {@link Observable} from the passed {@link rx.Observable.OnSubscribe}.
     *
     * The {@link Observable} is guaranteed to be well behaved and call the {@link Subscriber#onStart()},
     * {@link Subscriber#onError(Throwable)} and  {@link Subscriber#onCompleted()} methods as expected.
     * @param onNext subscription handler which will handle all states properly
     * @return new observable which will call the state functions as expected
     */
    public static <T> Observable<T> create(final UnsafeAction<T> onNext) {
        return Observable.create(new SafeOnSubscribe<>(onNext));
    }

    /**
     * Creates simple mapping function to be used with {@link Observable#flatMap(Func1)} and relies on
     * the proper handling on {@link SafeOnSubscribe}.
     * @param function generator function which takes the previous result as first argument and onNext notification function as the second
     * @param <T> type of the result from the source {@link Observable}
     * @param <R> type to be passes to onNext action
     * @return function to be used with {@link Observable#flatMap(Func1)} to map {@link Observable} items to different ones
     */
    public static <T, R> Func1<T, Observable<R>> safe(final UnsafeActionFunction<T, R> function) {
        return result -> create(new UnsafeAction<R>() {
            @Override
            public void call(Action1<R> onNext1) throws Throwable {
                function.call(result, onNext1);
            }
        });
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        subscriber.onStart();

        try {
            onNext.call(subscriber::onNext);
        } catch (Throwable th) {
            subscriber.onError(th);
        }

        subscriber.onCompleted();
    }

}
