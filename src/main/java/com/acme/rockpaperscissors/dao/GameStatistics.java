package com.acme.rockpaperscissors.dao;

import com.acme.rockpaperscissors.enums.GameResult;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "game_statistics")
public class GameStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_games_played")
    private Long totalGamesPlayed = 0L;

    @Column(name = "total_wins")
    private Long totalWins = 0L;

    @Column(name = "total_losses")
    private Long totalLosses = 0L;

    @Column(name = "total_draws")
    private Long totalDraws = 0L;

    @Column(name = "total_quits")
    private Long totalQuits = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateStatistics(GameResult result) {
        this.totalGamesPlayed++;
        switch (result) {
            case WIN -> this.totalWins++;
            case LOSE -> this.totalLosses++;
            case DRAW -> this.totalDraws++;
        }
    }

    public void incrementQuits() {
        this.totalQuits++;
    }
}
