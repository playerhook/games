package org.playerhook.games.tictactoe.hook.springboot

import groovy.json.JsonOutput
import org.junit.Test
import org.junit.runner.RunWith
import org.playerhook.games.api.LocalSession
import org.playerhook.games.api.Player
import org.playerhook.games.api.Position
import org.playerhook.games.api.SessionUpdate
import org.playerhook.games.api.SessionUpdateType
import org.playerhook.games.api.Token
import org.playerhook.games.api.TokenPlacement
import org.playerhook.games.tictactoe.TicTacToeRules
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.security.SecureRandom

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RandomController)
@RunWith(SpringRunner)
public class RandomControllerTest extends Specification {

    @Autowired MockMvc mvc

    @Test void "test hook"() {
        SecureRandom random = new SecureRandom()
        LocalSession session = TicTacToeRules.matchThree(null, new URL("http://private-f8637-playerhook.apiary-mock.com/games/session/xyz"))

        Player dartagnan = Player.create('dartagnan')
        Player athos = Player.create('athos')

        session.join(dartagnan)
        session.join(athos)

        session.start()

        for (int i = 0; i < 5; i++) {
            Player onTurn = session.playerOnTurn.get()
            int nextRow = session.board.firstRow + random.nextInt(session.board.height)
            int nextCol = session.board.firstColumn + random.nextInt(session.board.width)
            Token token = session.getDeck(onTurn).playableTokens.first()
            session.play(TokenPlacement.create(token, onTurn, Position.at(nextRow, nextCol)))
        }

        when:
            String jsonSession = JsonOutput.toJson(SessionUpdate.of(session, SessionUpdateType.Default.MOVE).toMap());
            this.mvc.perform(post("/tictactoe/random?u=${session.playerOnTurn.get().username}").content(jsonSession).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted()).andExpect(content().json('{"acknowledged": true}'))
        then:
            noExceptionThrown()
    }
}
