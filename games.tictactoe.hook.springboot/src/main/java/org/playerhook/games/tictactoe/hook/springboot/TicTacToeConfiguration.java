package org.playerhook.games.tictactoe.hook.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication @ComponentScan
public class TicTacToeConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(TicTacToeConfiguration.class, args);
    }

}
