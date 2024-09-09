package com.acme.rockpaperscissors.dao;

import com.acme.rockpaperscissors.enums.GameResult;
import com.acme.rockpaperscissors.enums.GameStatus;
import com.acme.rockpaperscissors.enums.Move;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rock_paper_scissors")
@Data
@NoArgsConstructor
public class RockPaperScissorsGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Move playerMove;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Move computerMove;

    @Column
    private GameResult result;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_status", nullable = false)
    private GameStatus gameStatus = GameStatus.NOT_STARTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
