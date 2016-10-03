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
                Object map = it.toMap()
                String json = JsonOutput.prettyPrint(JsonOutput.toJson(map))
                SessionUpdate update = materialize(new JsonSlurper().parseText(json)) { url, tokenPlacement -> }
                String other = JsonOutput.prettyPrint(JsonOutput.toJson(update.toMap()))
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

    @SuppressWarnings(['Println', 'AbcMetric', 'NestedForLoop'])
    private static void printSession(SessionUpdate sessionUpdate) {
        Session session = sessionUpdate.session

        println '=' * 60
        println session.game.title.center(60)

        if (session.playedMoves && session.playedMoves.last().ruleViolation.isPresent()) {
            println '-' * 60
            println session.playedMoves.last().ruleViolation.get().message.center(60, '!')
        }

        println '=' * 60
        session.players.each {
            if (it == session.playerOnTurn.orElse(null)) {
                print '* '
            } else {
                print '  '
            }
            print it.username.padRight(40)
            print ': '
            print String.valueOf(session.getScore(it)).padLeft(5, '0')
            println()
        }
        println '=' * 60
        println '-' * (session.board.width * 2 + 1)
        for (int row = session.board.firstRow; row <= session.board.lastRow; row++) {
            print '|'
            for (int col = session.board.firstColumn; col <= session.board.lastColumn; col++) {
                Optional<TokenPlacement> optional = session.board.getTokenPlacement(Position.at(row, col))
                if (optional.isPresent()) {
                    TokenPlacement tokenPlacement = optional.get()
                    String symbol = tokenPlacement.token.symbol
                    if (session.playedMoves && session.playedMoves.last().tokenPlacement == tokenPlacement) {
                        symbol = symbol.toUpperCase()
                    }
                    print symbol
                } else {
                    print ' '
                }
                print '|'
            }
            println()
            println '-' * (session.board.width * 2 + 1)
        }
        println '-' * 60
        println "Updated: ${sessionUpdate.type}"
    }

}
