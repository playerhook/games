package org.playerhook.games.stupid.hooks.springboot;

import org.playerhook.games.api.SessionUpdate;
import org.playerhook.games.util.Acknowledgement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/random")
public class RandomController {

    private final GameService gameService;

    @Autowired
    public RandomController(GameService gameService) {
        this.gameService = gameService;
    }

    @RequestMapping(method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    Acknowledgement playIfOnTurn(@RequestBody String body,
                                 @RequestParam("u") String username,
                                 @RequestHeader(name = "X-PlayerHook-Player-Key", required = false) String key) {
        SessionUpdate update = SessionUpdate.materialize(new JacksonJsonParser().parseMap(body));
        gameService.playIfOnTurn(update, username, key);
        return Acknowledgement.ACKNOWLEDGED;
    }

}
