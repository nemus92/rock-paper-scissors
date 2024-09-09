package com.acme.rockpaperscissors.service;

import com.acme.rockpaperscissors.dao.User;
import com.acme.rockpaperscissors.enums.GameResult;

public interface StatisticsService {
    String observeStatistics(String username);

    void updateStatistics(GameResult result, User user);
}
