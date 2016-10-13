package org.playerhook.games.api

import com.google.common.collect.ImmutableList
import spock.lang.Specification

/**
 * Tests for the board.
 */
class BoardSpec extends Specification {

    void 'Various way how to build board'() {
        expect:
            Board.rectangle(0, 5, 0, 5) == Board.square(5)
            Board.rectangle(3, 6, 3, 6) == Board.square(3, 6)
            Board.rectangle(7, 7, ImmutableList.of()) == Board.square(7, ImmutableList.of())
    }

    void 'Various dimensions'() {
        when:
            Board board = Board.rectangle(0, 5, 3, 8)
        then:
            with(board) {
                firstColumn == 0
                width == 5
                firstRow == 3
                height == 8
                lastColumn == 4
                lastRow == 10
                !completelyFilled
            }
    }

    void 'Place some tokens'() {
        when:
            Board board = Board.square(1)
            Player player = Player.create('tester')
            TokenPlacement placement = TokenPlacement.create(Token.HIDDEN, player, Position.at(0, 0))
            board = board.place(placement)
        then:
            board.completelyFilled
            board.contains(Position.at(0, 0))
            board.tokenPlacements.size() == 1
    }

}
