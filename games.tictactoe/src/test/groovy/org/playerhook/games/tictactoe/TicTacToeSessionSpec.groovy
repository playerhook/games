package org.playerhook.games.tictactoe

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.playerhook.games.api.LocalSession
import org.playerhook.games.api.Player
import org.playerhook.games.api.Position
import org.playerhook.games.api.Session
import org.playerhook.games.api.SessionUpdate
import org.playerhook.games.api.Token
import org.playerhook.games.api.TokenPlacement
import org.playerhook.games.util.SessionPrinter
import spock.lang.Specification

import java.security.SecureRandom

import static org.playerhook.games.api.SessionUpdate.materialize

/**
 * Tic Tac Toe game specification.
 */
class TicTacToeSessionSpec extends Specification {

    void "sanity check"() {
        when:
            LocalSession session = TicTacToeRules.matchThree(
                    new URL('http://www.example.com/ttt'),
                    new URL('http://www.example.com/ttt/123')
            )

            session.asObservable().subscribe {
                printSession(it)
            }

            Player dartagnan = Player.create('dartagnan')
            Player athos = Player.create('athos')

            session.join(dartagnan)
            session.join(athos)

            session.start()

            finish(session)

        then:
            noExceptionThrown()
    }

    void "sanity check - to map"() {
        when:
            LocalSession session = TicTacToeRules.matchThree(
                    new URL('http://www.example.com/ttt'),
                    new URL('http://www.example.com/ttt/123')
            )

            session.asObservable().subscribe {
                Object map = it.toMap(false)
                String json = JsonOutput.prettyPrint(JsonOutput.toJson(map))
                SessionUpdate update = materialize(new JsonSlurper().parseText(json)) { url, tokenPlacement -> }
                String other = JsonOutput.prettyPrint(JsonOutput.toJson(update.toMap(false)))
                assert json == other
            }

            Player dartagnan = Player.create('dartagnan')
            Player athos = Player.create('athos')

            session.join(dartagnan)
            session.join(athos)

            session.start()

            finish(session)

        then:
            noExceptionThrown()
    }

    protected static void finish(Session session) {
        SecureRandom random = new SecureRandom()
        while (!session.finished) {
            Player onTurn = session.playerOnTurn.get()
            int nextRow = session.board.firstRow + random.nextInt(session.board.height)
            int nextCol = session.board.firstColumn + random.nextInt(session.board.width)
            Token token = session.getDeck(onTurn).playableTokens.first()
            session.play(TokenPlacement.create(token, onTurn, Position.at(nextRow, nextCol)))
        }
    }

    private static void printSession(SessionUpdate sessionUpdate) {
        SessionPrinter.out().print(sessionUpdate)
    }

}
