package com.acme.rockpaperscissors.utils;

import com.acme.rockpaperscissors.enums.Move;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MovePredictor {

    public Move predictNextMove(Map<Move, Integer> moveCount) {
        int totalMoves = moveCount.values().stream().mapToInt(Integer::intValue).sum();
        if (totalMoves < 5) {
            // Default initial move when insufficient data
            return Move.ROCK;
        }

        return moveCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(Move.ROCK);
    }
}
