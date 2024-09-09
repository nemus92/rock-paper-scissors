package com.acme.rockpaperscissors.controller;

import com.acme.rockpaperscissors.enums.Move;
import com.acme.rockpaperscissors.exception.GameSaveException;
import com.acme.rockpaperscissors.exception.InvalidMoveException;
import com.acme.rockpaperscissors.service.RockPaperScissorsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/rps")
public class RockPaperScissorsController {

    private final RockPaperScissorsService rockPaperScissorsService;

    public RockPaperScissorsController(RockPaperScissorsService rockPaperScissorsService) {
        this.rockPaperScissorsService = rockPaperScissorsService;
    }

    @Operation(summary = "Start a new game")
    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestParam @NotNull String username) {
        try {
            String response = rockPaperScissorsService.startGame(username);
            return ResponseEntity.ok(response);
        } catch (InvalidMoveException | GameSaveException | IllegalArgumentException ex) {
            throw ex;
        }
    }

    @Operation(summary = "Play a move")
    @PostMapping("/play")
    public ResponseEntity<String> play(@RequestParam Move playerMove, @RequestParam String username) {
        try {
            String response = rockPaperScissorsService.play(playerMove, username);
            return ResponseEntity.ok(response);
        } catch (InvalidMoveException | GameSaveException | IllegalArgumentException ex) {
            throw ex;
        }
    }

    @Operation(summary = "Pause the current game")
    @PostMapping("/pause")
    public ResponseEntity<String> pauseGame(@RequestParam String username) {
        try {
            String response = rockPaperScissorsService.pauseGame(username);
            return ResponseEntity.ok(response);
        } catch (InvalidMoveException | GameSaveException | IllegalArgumentException ex) {
            throw ex;
        }
    }

    @Operation(summary = "Resume the paused game")
    @PostMapping("/resume")
    public ResponseEntity<String> resumeGame(@RequestParam String username) {
        try {
            String response = rockPaperScissorsService.resumeGame(username);
            return ResponseEntity.ok(response);
        } catch (InvalidMoveException | GameSaveException | IllegalArgumentException ex) {
            throw ex;
        }
    }

    @Operation(summary = "Terminate the current game")
    @PostMapping("/terminate")
    public ResponseEntity<String> terminateGame(@RequestParam String username) {
        try {
            String response = rockPaperScissorsService.terminateGame(username);
            return ResponseEntity.ok(response);
        } catch (InvalidMoveException | GameSaveException | IllegalArgumentException ex) {
            throw ex;
        }
    }
}
