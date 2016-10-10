package org.playerhook.games.tictactoe

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.playerhook.games.api.LocalSession
import org.playerhook.games.api.Player
import org.playerhook.games.api.Position
import org.playerhook.games.api.SessionUpdate
import org.playerhook.games.api.Token
import org.playerhook.games.util.SessionPrinter
import spock.lang.Specification

import java.security.SecureRandom

import static org.playerhook.games.api.SessionUpdate.materialize
import static org.playerhook.games.util.MapSerializable.PrivacyLevel.PROTECTED

/**
 * Tic Tac Toe game specification.
 */
class TicTacToeSessionSpec extends Specification {

    void "sanity check"() {
        when:
            LocalSession session = TicTacToeRules.matchThree(
                    new URL('http://www.example.com/ttt'),
                    new URL('http://www.example.com/ttt/123')
            ).asObservableSession()

            session.observe().subscribe {
                printSession(it)
            }

            Player dartagnan = Player.create('dartagnan')
            Player athos = Player.create('athos')

            finish(session.join(dartagnan).join(athos).signWith('pa$$word').start())

        then:
            noExceptionThrown()
    }

    void "sanity check - to map"() {
        when:
            LocalSession session = TicTacToeRules.matchThree(
                    new URL('http://www.example.com/ttt'),
                    new URL('http://www.example.com/ttt/345')
            ).asObservableSession()

            session.observe().subscribe {
                Object map = it.toMap(PROTECTED)
                String json = JsonOutput.prettyPrint(JsonOutput.toJson(map))
                SessionUpdate update = materialize(new JsonSlurper().parseText(json))
                String other = JsonOutput.prettyPrint(JsonOutput.toJson(update.toMap(PROTECTED)))
                assert json == other
            }

            Player dartagnan = Player.create('dartagnan')
            Player athos = Player.create('athos')

            finish(session.join(dartagnan).join(athos).start())

        then:
            noExceptionThrown()
    }

    protected static void finish(LocalSession session) {
        LocalSession s = session
        SecureRandom random = new SecureRandom()
        while (!s.finished) {
            Player onTurn = s.playerOnTurn.get()
            int nextRow = s.board.firstRow + random.nextInt(s.board.height)
            int nextCol = s.board.firstColumn + random.nextInt(s.board.width)
            Token token = s.getDeck(onTurn).playableTokens.first()
            s = s.play(s.sign(s.newPlacement(token, onTurn, Position.at(nextRow, nextCol))))
        }
    }

    private static void printSession(SessionUpdate sessionUpdate) {
        SessionPrinter.out().print(sessionUpdate)
    }

}
