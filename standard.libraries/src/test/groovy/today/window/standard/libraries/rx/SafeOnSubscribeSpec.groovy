package today.window.standard.libraries.rx

import rx.Observable
import rx.Subscriber
import spock.lang.Specification

/**
 * Specification for SafeOnSubscribe helper class.
 */
class SafeOnSubscribeSpec extends Specification {

    void "on start and on completed is called properly"() {
        Observable<Boolean> observable = SafeOnSubscribe.create {
            it(true)
        }

        when:
        observable.toBlocking().first()
        then:
        noExceptionThrown()
    }

    void "all methods are called"() {
        Observable<Boolean> observable = SafeOnSubscribe.create {
            it(true)
        }

        Subscriber<Boolean> subscriber = Mock(Subscriber)

        when:
        observable.subscribe(subscriber)

        then:
        0 * subscriber.onError(_)
        1 * subscriber.onStart()

        and:
        1 * subscriber.onNext(true)

        and:
        1 * subscriber.onCompleted()
    }

    void "exception is handled properly"() {
        Throwable thrown = new Throwable('Failed')
        Observable<Boolean> observable = SafeOnSubscribe.create {
            throw thrown
        }

        Subscriber<Boolean> subscriber = Mock(Subscriber)

        when:
        observable.subscribe(subscriber)

        then:
        1 * subscriber.onStart()
        1 * subscriber.onError(_ as Throwable)

        and:
        0 * subscriber.onNext(true)

        and:
        0 * subscriber.onCompleted()
    }

    void "simple mapping function"() {
        Observable<Boolean> observable = Observable.just(true).flatMap(SafeOnSubscribe.safe { source, onNext ->
            onNext(!source)
        })

        Subscriber<Boolean> subscriber = Mock(Subscriber)

        when:
        observable.subscribe(subscriber)

        then:
        0 * subscriber.onError(_)
        1 * subscriber.onStart()

        and:
        1 * subscriber.onNext(false)

        and:
        1 * subscriber.onCompleted()
    }

    void "simple mapping function with error"() {
        Throwable thrown = new Throwable('Failed')
        Observable<Boolean> observable = Observable.just(true).flatMap(SafeOnSubscribe.safe { source, onNext ->
            throw thrown
        })

        Subscriber<Boolean> subscriber = Mock(Subscriber)

        when:
        observable.subscribe(subscriber)

        then:
        1 * subscriber.onStart()
        1 * subscriber.onError(thrown)

        and:
        0 * subscriber.onNext(true)

        and:
        0 * subscriber.onCompleted()
    }
}
