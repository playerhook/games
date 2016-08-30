package org.playerhook.games.tictactoe.hook.springboot;

import org.playerhook.games.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tictactoe/random")
public class RandomController {

    @Autowired PlayService playService;

    @RequestMapping(method=RequestMethod.POST)
    public @ResponseBody
    Acknowledgement playIfOnTurn(@RequestBody String body, @RequestParam("u") String username) {
        SessionUpdate update = SessionUpdate.materialize(new BasicJsonParser().parseMap(body), playService::sendPlacement);
        playService.playIfOnTurn(update, username);
        return Acknowledgement.ACKNOWLEDGED;
    }

}
