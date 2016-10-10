package org.playerhook.games.stupid.hooks.springboot

import org.junit.Test
import org.junit.runner.RunWith
import org.playerhook.games.api.LocalSession
import org.playerhook.games.api.Player
import org.playerhook.games.api.Position
import org.playerhook.games.api.SessionUpdateType
import org.playerhook.games.api.Token
import org.playerhook.games.tictactoe.TicTacToeRules
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.security.SecureRandom

import static groovy.json.JsonOutput.toJson
import static org.playerhook.games.api.SessionUpdate.of
import static org.playerhook.games.util.MapSerializable.PrivacyLevel.PROTECTED
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Test for random controller
 */
@WebMvcTest(RandomController)
@RunWith(SpringRunner)
class RandomControllerSpec extends Specification {

    @Autowired MockMvc mvc

    @Test void "test hook"() {
        SecureRandom random = new SecureRandom()

        Player dartagnan = Player.create('dartagnan')
        Player athos = Player.create('athos')

        LocalSession session = TicTacToeRules
            .matchThree(null, new URL('http://private-f8637-playerhook.apiary-mock.com/games/session/xyz'))
            .join(dartagnan).join(athos).start()

        for (int i = 0; i < 5; i++) {
            Player onTurn = session.playerOnTurn.get()
            int nextRow = session.board.firstRow + random.nextInt(session.board.height)
            int nextCol = session.board.firstColumn + random.nextInt(session.board.width)
            Token token = session.getDeck(onTurn).playableTokens.first()
            session = session.play(session.newPlacement(token, onTurn, Position.at(nextRow, nextCol)))
        }

        when:
            String jsonSession = toJson(of(session, SessionUpdateType.Default.MOVE).toMap(PROTECTED))

            this.mvc.perform(
                    post("/random?u=${session.playerOnTurn.get().username}")
                    .content(jsonSession)
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(
                    status().isAccepted()
            ).andExpect(
                    content().json('{"acknowledged": true}')
            )
        then:
            noExceptionThrown()
    }
}
