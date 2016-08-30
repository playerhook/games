package org.playerhook.games.tictactoe.hook.springboot;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.api.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class GameService {

    public void sendPlacement(URL url, TokenPlacement placement) {
        new RestTemplate().postForObject(url.toExternalForm(), placement.toMap(), Acknowledgement.class);
    }

    public void playIfOnTurn(SessionUpdate update, String username) {
        Session session = update.getSession();

        if (!Status.IN_PROGRESS.equals(session.getStatus())) {
            // only play while in progress
            return;
        }

        Optional<Player> playerOnTurn = session.getPlayerOnTurn();

        if (!playerOnTurn.isPresent()) {
            // only if someone is on turn
            return;
        }

        Player player = session.getPlayers().stream()
            .filter(p -> username.equals(p.getUsername()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Current player isn't part at the game"));

        if (!playerOnTurn.get().getUsername().equals(username)) {
            // only if the username matches the user on turn
            return;
        }

        Deck deck = session.getDeck(player);

        ImmutableList<Token> playableTokens = deck.getPlayableTokens();

        if (playableTokens.size() == 0) {
            // play only if you have some tokens to play
            return;
        }

        Token token = playableTokens.get(0);

        SecureRandom random = new SecureRandom();

        int nextRow = session.getBoard().getFirstRow() + random.nextInt(session.getBoard().getHeight());
        int nextCol = session.getBoard().getFirstColumn() + random.nextInt(session.getBoard().getHeight());
        session.play(TokenPlacement.create(token, player, Position.at(nextRow, nextCol)));
    }
}
