package com.acme.rockpaperscissors.service.impl;

import com.acme.rockpaperscissors.dao.RockPaperScissorsGame;
import com.acme.rockpaperscissors.dao.User;
import com.acme.rockpaperscissors.enums.GameResult;
import com.acme.rockpaperscissors.enums.GameStatus;
import com.acme.rockpaperscissors.enums.Move;
import com.acme.rockpaperscissors.exception.GameSaveException;
import com.acme.rockpaperscissors.repository.RockPaperScissorsRepository;
import com.acme.rockpaperscissors.repository.UserRepository;
import com.acme.rockpaperscissors.service.StatisticsService;
import com.acme.rockpaperscissors.service.UserService;
import com.acme.rockpaperscissors.utils.MovePredictor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static com.acme.rockpaperscissors.service.impl.RockPaperScissorsServiceImpl.EXCLUDED_STATUSES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class RockPaperScissorsServiceImplTest {

    @InjectMocks
    private RockPaperScissorsServiceImpl rockPaperScissorsService;

    @Mock
    private RockPaperScissorsRepository rockPaperScissorsRepository;

    @Mock
    private MovePredictor movePredictor;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private UserService userService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStartGame_whenNoExistingGame() {
        User user = new User();
        user.setUsername("testUser");

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUser(user)).thenReturn(Optional.empty());

        String response = rockPaperScissorsService.startGame("testUser");

        assertEquals("New game started for testUser. Make your first move!", response);
        verify(rockPaperScissorsRepository).save(any(RockPaperScissorsGame.class));
    }

    @Test
    void testStartGame_whenGameAlreadyStarted() {
        // Arrange
        User user = new User();
        user.setUsername("testUser");

        RockPaperScissorsGame game = new RockPaperScissorsGame();
        game.setGameStatus(GameStatus.STARTED);

        when(userService.findOrCreateUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)).thenReturn(Optional.of(game));

        String response = rockPaperScissorsService.startGame("testUser");
        assertEquals("A game is already in progress for testUser. Make your next move!", response);
    }


    @Test
    void testPlay_validMove() {
        User user = new User();
        user.setUsername("testUser");
        RockPaperScissorsGame game = new RockPaperScissorsGame();
        game.setGameStatus(GameStatus.STARTED);

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)).thenReturn(Optional.of(game));
        when(movePredictor.predictNextMove(any())).thenReturn(Move.ROCK);

        String response = rockPaperScissorsService.play(Move.PAPER, "testUser");

        assertTrue(response.contains("Computer predicted ROCK and chose"));
        verify(rockPaperScissorsRepository).save(any(RockPaperScissorsGame.class));
        verify(statisticsService).updateStatistics(any(GameResult.class), eq(user));
    }

    @Test
    void testPlay_invalidMove() {
        User user = new User();
        user.setUsername("testUser");
        RockPaperScissorsGame game = new RockPaperScissorsGame();
        game.setGameStatus(GameStatus.STARTED);

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)).thenReturn(Optional.of(game));

        String response = rockPaperScissorsService.play(null, "testUser");

        assertEquals("Game terminated. Player has quit.", response);
        verify(statisticsService).updateStatistics(GameResult.QUIT, user);
    }

    @Test
    void testPauseGame() {
        User user = new User();
        user.setUsername("testUser");
        RockPaperScissorsGame game = new RockPaperScissorsGame();
        game.setGameStatus(GameStatus.STARTED);

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)).thenReturn(Optional.of(game));

        String response = rockPaperScissorsService.pauseGame("testUser");

        assertEquals("Game paused for testUser.", response);
        verify(rockPaperScissorsRepository).save(game);
    }

    @Test
    void testResumeGame() {
        User user = new User();
        user.setUsername("testUser");
        RockPaperScissorsGame game = new RockPaperScissorsGame();
        game.setGameStatus(GameStatus.PAUSED);

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)).thenReturn(Optional.of(game));

        String response = rockPaperScissorsService.resumeGame("testUser");

        assertEquals("Game resumed for testUser. Make your next move!", response);
        verify(rockPaperScissorsRepository).save(game);
    }

    @Test
    void testTerminateGame() {
        User user = new User();
        user.setUsername("testUser");
        RockPaperScissorsGame game = new RockPaperScissorsGame();
        game.setGameStatus(GameStatus.PAUSED);

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        when(rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)).thenReturn(Optional.of(game));

        String response = rockPaperScissorsService.terminateGame("testUser");

        assertEquals("Game terminated for user testUser.", response);
        verify(statisticsService).updateStatistics(GameResult.QUIT, user);
    }

    @Test
    void testSaveGameData_exception() {
        User user = new User();
        user.setUsername("testUser");

        doThrow(new RuntimeException("DB error")).when(rockPaperScissorsRepository).save(any(RockPaperScissorsGame.class));

        assertThrows(GameSaveException.class, () -> {
            rockPaperScissorsService.saveGameData(Move.ROCK, Move.PAPER, GameResult.WIN, user);
        });
    }
}
