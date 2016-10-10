package org.playerhook.games.api

import org.playerhook.games.tictactoe.TicTacToeRules
import spock.lang.Specification

/**
 * Specificaton for Game value type.
 */
class GameSpec extends Specification {

    void 'test equals'() {
        given:
            Game first = Game.of('Title', 'Description', new URL('http://ex.pl'), new TicTacToeRules.Three())
            Game second = Game.of('Title', 'Description', new URL('http://ex.pl'), new TicTacToeRules.Three())

        expect:
            first == second
    }
}
