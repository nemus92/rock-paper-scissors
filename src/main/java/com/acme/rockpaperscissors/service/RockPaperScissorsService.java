package com.acme.rockpaperscissors.service;

import com.acme.rockpaperscissors.enums.Move;

public interface RockPaperScissorsService {
    String startGame(String username);

    String play(Move playerMove, String username);

    String pauseGame(String username);

    String resumeGame(String username);

    String terminateGame(String username);
}
