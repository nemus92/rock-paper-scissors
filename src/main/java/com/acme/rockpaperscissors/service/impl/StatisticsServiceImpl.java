package com.acme.rockpaperscissors.service.impl;

import com.acme.rockpaperscissors.dao.GameStatistics;
import com.acme.rockpaperscissors.dao.User;
import com.acme.rockpaperscissors.enums.GameResult;
import com.acme.rockpaperscissors.exception.StatisticsNotFoundException;
import com.acme.rockpaperscissors.repository.GameStatisticsRepository;
import com.acme.rockpaperscissors.service.StatisticsService;
import com.acme.rockpaperscissors.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private GameStatisticsRepository gameStatisticsRepository;

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Override
    public String observeStatistics(String username) {
        User user = userService.findUserByUsername(username);

        GameStatistics stats = gameStatisticsRepository.findByUser(user)
            .orElseThrow(() -> new StatisticsNotFoundException(username));

        return String.format("User: %s - Wins: %d, Losses: %d, Draws: %d, Quits: %d",
            username, stats.getTotalWins(), stats.getTotalLosses(), stats.getTotalDraws(), stats.getTotalQuits());
    }

    @Override
    public void updateStatistics(GameResult result, User user) {
        try {
            GameStatistics stats = gameStatisticsRepository.findByUser(user)
                .orElseGet(() -> {
                    GameStatistics newStats = new GameStatistics();
                    newStats.setUser(user);
                    return newStats;
                });

            stats.updateStatistics(result);

            if (result == GameResult.QUIT) {
                stats.incrementQuits();
            }

            gameStatisticsRepository.save(stats);
            logger.info("Game statistics updated: {}", stats);
        } catch (Exception e) {
            logger.error("Failed to update game statistics", e);
        }
    }

//    @Override
//    public void createOrUpdateStatsForNewGame(User user) {
//        try {
//            GameStatistics stats = gameStatisticsRepository.findByUser(user)
//                .orElseGet(() -> {
//                    GameStatistics newStats = new GameStatistics();
//                    newStats.setUser(user);
//                    return newStats;
//                });
//
//            stats.setTotalGamesPlayed(stats.getTotalGamesPlayed() + 1);
//            gameStatisticsRepository.save(stats);
//        } catch (Exception e) {
//            logger.error("Failed to create or update game statistics for new game", e);
//        }
//    }
}
