package com.acme.rockpaperscissors.repository;

import com.acme.rockpaperscissors.dao.GameStatistics;
import com.acme.rockpaperscissors.dao.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameStatisticsRepository extends JpaRepository<GameStatistics, Long> {
    Optional<GameStatistics> findByUser(User user);
}
