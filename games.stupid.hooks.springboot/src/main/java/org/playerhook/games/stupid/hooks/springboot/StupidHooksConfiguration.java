package org.playerhook.games.stupid.hooks.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication @ComponentScan
public class StupidHooksConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(StupidHooksConfiguration.class, args);
    }

}
