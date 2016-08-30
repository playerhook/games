package org.playerhook.games.tictactoe.hook;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tictactoe/random")
public class RandomController {

    private static final String TEMPLATE = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(method=RequestMethod.POST)
    public @ResponseBody Greeting sayHello(@RequestBody String body, @RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        System.out.println(body);
        System.out.println(new BasicJsonParser().parseMap(body));
        return new Greeting(counter.incrementAndGet(), String.format(TEMPLATE, name));
    }

}
