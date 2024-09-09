package com.acme.rockpaperscissors.dto;

import com.acme.rockpaperscissors.enums.Move;

import java.time.LocalDateTime;

public class RockPaperScissorsDTO {
    private Move playerMove;
    private Move computerMove;
    private String result;
    private LocalDateTime timestamp;
}
