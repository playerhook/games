package org.playerhook.games.stupid.hooks.springboot;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.api.*;
import org.playerhook.games.util.Acknowledgement;
import org.playerhook.games.util.SessionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class GameService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void sendPlacement(URL url, TokenPlacement placement) {
        if (log.isInfoEnabled()) {
            log.info("Notifying session " + url + " with " + placement);
        }
        new RestTemplate().postForObject(url.toExternalForm(), placement.toMap(false), Acknowledgement.class);
    }

    public void playIfOnTurn(SessionUpdate update, String username) {
        if (log.isInfoEnabled()) {
            log.info("Processing session update for " + username + ": " + update + "\n" + SessionPrinter.toString(update));
        }
        Session session = update.getSession();

        if (!Status.IN_PROGRESS.equals(session.getStatus())) {
            if (log.isInfoEnabled()) {
                log.info("Session is no longer in progress: " + update);
            }
            return;
        }

        Optional<Player> playerOnTurn = session.getPlayerOnTurn();

        if (!playerOnTurn.isPresent()) {
            if (log.isInfoEnabled()) {
                log.info("There is no player on turn: " + update);
            }
            return;
        }

        Player player = session.getPlayers().stream()
            .filter(p -> username.equals(p.getUsername()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Current player " + username + " isn't part at the game: "
                    + session.getPlayers()));

        if (!playerOnTurn.get().getUsername().equals(username)) {
            if (log.isInfoEnabled()) {
                log.info("Current player " + username + " is not on turn: " + update);
            }
            return;
        }

        Deck deck = session.getDeck(player);

        ImmutableList<Token> playableTokens = deck.getPlayableTokens();

        if (playableTokens.size() == 0) {
            if (log.isInfoEnabled()) {
                log.info("No more moves for " + username + ": " + update);
            }
            return;
        }

        Token token = playableTokens.get(0);

        SecureRandom random = new SecureRandom();

        int nextRow = session.getBoard().getFirstRow() + random.nextInt(session.getBoard().getHeight());
        int nextCol = session.getBoard().getFirstColumn() + random.nextInt(session.getBoard().getHeight());
        session.play(TokenPlacement.create(token, player, Position.at(nextRow, nextCol)));
    }
}
