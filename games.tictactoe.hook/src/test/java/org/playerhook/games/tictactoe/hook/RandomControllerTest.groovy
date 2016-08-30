package org.playerhook.games.tictactoe.hook

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.playerhook.games.api.LocalSession
import org.playerhook.games.api.Player
import org.playerhook.games.api.Position
import org.playerhook.games.api.Token
import org.playerhook.games.api.TokenPlacement
import org.playerhook.games.tictactoe.TicTacToeSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.MockHttpOutputMessage
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
        LocalSession session = new TicTacToeSession(3, 3)

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
            session.play(TokenPlacement.of(token, onTurn, Position.of(nextRow, nextCol)))
        }

        when:
            String jsonSession = json(session)
            println jsonSession
            this.mvc.perform(post("/tictactoe/random").content(jsonSession).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json('{"id":1,"content":"Hello, Stranger!"}'))
        then:
            noExceptionThrown()
    }

}
