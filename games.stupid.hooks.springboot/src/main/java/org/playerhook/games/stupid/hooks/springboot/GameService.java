package org.playerhook.games.stupid.hooks.springboot;

import com.google.common.collect.ImmutableList;
import org.playerhook.games.api.*;
import org.playerhook.games.util.Acknowledgement;
import org.playerhook.games.util.MapSerializable;
import org.playerhook.games.util.SessionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class GameService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SecureRandom random = new SecureRandom();

    public void sendPlacement(URL url, TokenPlacement placement) {
        if (log.isInfoEnabled()) {
            log.info("Notifying session " + url + " with " + placement);
        }
        try {
            new RestTemplate().postForObject(url.toExternalForm(), placement.toMap(MapSerializable.PrivacyLevel.PROTECTED), Acknowledgement.class);
        } catch (RestClientException e) {
            log.error("Exception sending placement " + placement + " to " + url + ": " + e.toString());
        }
    }

    public void playIfOnTurn(SessionUpdate update, String username, String key) {
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

        session.getURL().ifPresent(url -> sendPlacement(url, randomPlacement(key, session, player, token)));
    }

    private TokenPlacement randomPlacement(String key, Session session, Player player, Token token) {
        int counter = 0;
        while (++counter <= 100) {
            int nextRow = session.getBoard().getFirstRow() + random.nextInt(session.getBoard().getHeight());
            int nextCol = session.getBoard().getFirstColumn() + random.nextInt(session.getBoard().getHeight());
            if (!session.getBoard().getTokenPlacement(Position.at(nextRow, nextCol)).isPresent()
                    || session.getBoard().isCompletelyFilled()
                    || counter == 100
            ) {
                return session.newPlacement(token, player, Position.at(nextRow, nextCol)).sign(key);
            }
        }
        throw new IllegalStateException("Problem retrieving next random move after 100 attemps!");
    }
}
