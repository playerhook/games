package org.playerhook.games.stupid.hooks.springboot;

import org.playerhook.games.api.*;
import org.playerhook.games.util.Acknowledgement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tictactoe/random")
public class RandomController {

    @Autowired
    GameService gameService;

    @RequestMapping(method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    Acknowledgement playIfOnTurn(@RequestBody String body, @RequestParam("u") String username) {
        SessionUpdate update = SessionUpdate.materialize(new BasicJsonParser().parseMap(body), gameService::sendPlacement);
        gameService.playIfOnTurn(update, username);
        return Acknowledgement.ACKNOWLEDGED;
    }

}
