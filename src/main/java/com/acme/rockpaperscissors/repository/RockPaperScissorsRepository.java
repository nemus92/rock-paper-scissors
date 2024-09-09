package com.acme.rockpaperscissors.repository;

import com.acme.rockpaperscissors.dao.RockPaperScissorsGame;
import com.acme.rockpaperscissors.dao.User;
import com.acme.rockpaperscissors.enums.GameStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RockPaperScissorsRepository extends JpaRepository<RockPaperScissorsGame, Long> {
    Optional<RockPaperScissorsGame> findByUser(User user);

    @Query("SELECT g FROM RockPaperScissorsGame g WHERE g.user = :user AND g.gameStatus NOT IN :excludedStatuses")
    Optional<RockPaperScissorsGame> findByUserAndGameStatusNot(@Param("user") User user, @Param("excludedStatuses") List<GameStatus> excludedStatuses);
}
