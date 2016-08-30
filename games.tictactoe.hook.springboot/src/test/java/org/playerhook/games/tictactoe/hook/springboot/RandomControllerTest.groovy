package org.playerhook.games.tictactoe.hook.springboot

import org.junit.Test
import org.junit.runner.RunWith
import org.playerhook.games.api.LocalSession
import org.playerhook.games.api.Player
import org.playerhook.games.api.Position
import org.playerhook.games.api.SessionUpdate
import org.playerhook.games.api.SessionUpdateType
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
        LocalSession session = new TicTacToeSession(3, 3, new URL("http://www.example.com/game/session/xyz"))

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
            String jsonSession = json(SessionUpdate.of(session, SessionUpdateType.Default.MOVE).toMap());
            this.mvc.perform(post("/tictactoe/random?u=dartagnan").content(jsonSession).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json('{"acknowledged": true}'))
        then:
            noExceptionThrown()
    }


    private HttpMessageConverter mappingJackson2HttpMessageConverter

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {
        mappingJackson2HttpMessageConverter = Arrays.asList(converters)
            .stream().filter{ hmc -> hmc instanceof MappingJackson2HttpMessageConverter}.findAny().get()

        assert this.mappingJackson2HttpMessageConverter
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
            o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
