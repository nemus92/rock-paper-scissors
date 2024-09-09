package com.acme.rockpaperscissors.service.impl;

import com.acme.rockpaperscissors.dao.RockPaperScissorsGame;
import com.acme.rockpaperscissors.dao.User;
import com.acme.rockpaperscissors.enums.GameResult;
import com.acme.rockpaperscissors.enums.GameStatus;
import com.acme.rockpaperscissors.enums.Move;
import com.acme.rockpaperscissors.exception.InvalidMoveException;
import com.acme.rockpaperscissors.exception.GameSaveException;
import com.acme.rockpaperscissors.repository.RockPaperScissorsRepository;
import com.acme.rockpaperscissors.service.RockPaperScissorsService;
import com.acme.rockpaperscissors.service.StatisticsService;
import com.acme.rockpaperscissors.service.UserService;
import com.acme.rockpaperscissors.utils.MovePredictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RockPaperScissorsServiceImpl implements RockPaperScissorsService {

    private static final Logger logger = LoggerFactory.getLogger(RockPaperScissorsServiceImpl.class);

    private final Map<Move, Integer> moveCount = new ConcurrentHashMap<>();
    private final RockPaperScissorsRepository rockPaperScissorsRepository;
    private final UserService userService;
    private final MovePredictor movePredictor;
    private final StatisticsService statisticsService;

    static final List<GameStatus> EXCLUDED_STATUSES = List.of(GameStatus.COMPLETED, GameStatus.TERMINATED);

    public RockPaperScissorsServiceImpl(RockPaperScissorsRepository rockPaperScissorsRepository,
        UserService userService,
        MovePredictor movePredictor,
        StatisticsService statisticsService) {
        this.rockPaperScissorsRepository = rockPaperScissorsRepository;
        this.userService = userService;
        this.movePredictor = movePredictor;
        this.statisticsService = statisticsService;

        // Initialize move counts for each Move type
        for (Move move : Move.values()) {
            moveCount.put(move, 0);
        }
    }

    @Override
    public String startGame(String username) {
        User user = userService.findOrCreateUserByUsername(username);

        Optional<RockPaperScissorsGame> optionalGame = rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES);

        if (optionalGame.isPresent() && optionalGame.get().getGameStatus() == GameStatus.STARTED) {
            logger.info("Game already in progress for user: {}", username);
            return "A game is already in progress for " + username + ". Make your next move!";
        } else {
            RockPaperScissorsGame newGame = new RockPaperScissorsGame();
            newGame.setUser(user);
            newGame.setComputerMove(Move.UNDEFINED);
            newGame.setPlayerMove(Move.UNDEFINED);
            newGame.setGameStatus(GameStatus.STARTED);
            rockPaperScissorsRepository.save(newGame);
            logger.info("New game created and started for user: {}", username);
            return "New game started for " + username + ". Make your first move!";
        }
    }

    @Override
    public String play(Move playerMove, String username) {
        User user = userService.findUserByUsername(username);

        RockPaperScissorsGame game = rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)
            .orElseThrow(() -> new IllegalArgumentException("No in-progress game found for user: " + username));

        if (game.getGameStatus() != GameStatus.STARTED) {
            return "The game is currently not started or paused. Start or resume the game to play.";
        }

        try {
            if (playerMove == null) {
                logger.warn("Player quit the game.");
                game.setGameStatus(GameStatus.TERMINATED);
                saveGameData(Move.UNDEFINED, Move.UNDEFINED, GameResult.QUIT, user);
                statisticsService.updateStatistics(GameResult.QUIT, user);
                return "Game terminated. Player has quit.";
            }

            if (playerMove == Move.UNDEFINED) {
                logger.warn("Player made an undefined move.");
                throw new InvalidMoveException("Invalid move. Choose rock, paper, or scissors.");
            }

            if (!moveCount.containsKey(playerMove)) {
                logger.warn("Invalid move attempt: {}", playerMove);
                throw new InvalidMoveException("Invalid move. Choose rock, paper, or scissors.");
            }

            // Update move counts for prediction
            moveCount.put(playerMove, moveCount.get(playerMove) + 1);

            // Use the MovePredictor to predict the player's next move
            Move predictedMove = movePredictor.predictNextMove(moveCount);
            logger.info("Predicted player's next move: {}", predictedMove);

            // Choose a counter move based on the predicted move
            Move computerMove = getCounterMove(predictedMove);
            GameResult result = determineResult(playerMove, computerMove);
            logger.info("Computer move: {}. Game result: {}", computerMove, result);

            // Save game data and update statistics
            saveGameData(playerMove, computerMove, result, user);
            statisticsService.updateStatistics(result, user);

            return String.format("Computer predicted %s and chose %s. You %s!", predictedMove, computerMove, result);

        } catch (InvalidMoveException e) {
            logger.error("Invalid move exception: {}", e.getMessage());
            return e.getMessage();
        } catch (GameSaveException e) {
            logger.error("Error saving game data: {}", e.getMessage());
            return "An error occurred while saving the game data.";
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return "An unexpected error occurred: " + e.getMessage();
        }
    }

    @Override
    public String pauseGame(String username) {
        User user = userService.findUserByUsername(username);

        RockPaperScissorsGame game = rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)
            .orElseThrow(() -> new IllegalArgumentException("No in-progress game found for user: " + username));


        if (game.getGameStatus() == GameStatus.STARTED) {
            game.setGameStatus(GameStatus.PAUSED);
            rockPaperScissorsRepository.save(game);
            logger.info("Game paused for user: {}", username);
            return "Game paused for " + username + ".";
        } else {
            return "The game is not in a started state, so it cannot be paused.";
        }
    }

    @Override
    public String resumeGame(String username) {
        User user = userService.findUserByUsername(username);

        RockPaperScissorsGame game = rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)
            .orElseThrow(() -> new IllegalArgumentException("No in-progress game found for user: " + username));

        if (game.getGameStatus() == GameStatus.PAUSED) {
            game.setGameStatus(GameStatus.STARTED);
            rockPaperScissorsRepository.save(game);
            logger.info("Game resumed for user: {}", username);
            return "Game resumed for " + username + ". Make your next move!";
        } else {
            return "The game is not paused, so it cannot be resumed.";
        }
    }

    @Override
    public String terminateGame(String username) {
        User user = userService.findUserByUsername(username);

        RockPaperScissorsGame game = rockPaperScissorsRepository.findByUserAndGameStatusNot(user, EXCLUDED_STATUSES)
            .orElseThrow(() -> new IllegalArgumentException("No in-progress game found for user: " + username));

        game.setGameStatus(GameStatus.TERMINATED);
        saveGameData(Move.UNDEFINED, Move.UNDEFINED, GameResult.QUIT, user);
        statisticsService.updateStatistics(GameResult.QUIT, user);

        logger.info("Game terminated for user: {}", username);
        return "Game terminated for user " + username + ".";
    }

    private Move getCounterMove(Move move) {
        return switch (move) {
            case ROCK -> Move.PAPER;
            case PAPER -> Move.SCISSORS;
            case SCISSORS -> Move.ROCK;
            default -> throw new IllegalStateException("Cannot get counter move for undefined move.");
        };
    }

    private GameResult determineResult(Move playerMove, Move computerMove) {
        if (playerMove.equals(computerMove)) {
            return GameResult.DRAW;
        } else if ((playerMove == Move.ROCK && computerMove == Move.SCISSORS) ||
                       (playerMove == Move.PAPER && computerMove == Move.ROCK) ||
                       (playerMove == Move.SCISSORS && computerMove == Move.PAPER)) {
            return GameResult.WIN;
        } else {
            return GameResult.LOSE;
        }
    }

    protected void saveGameData(Move playerMove, Move computerMove, GameResult result, User user) {
        try {
            RockPaperScissorsGame game = new RockPaperScissorsGame();
            game.setPlayerMove(playerMove);
            game.setComputerMove(computerMove);
            game.setResult(result);
            game.setUser(user);
            game.setGameStatus(GameStatus.COMPLETED);
            rockPaperScissorsRepository.save(game);
            logger.info("Game data saved: Player move: {}, Computer move: {}, Result: {}", playerMove, computerMove, result);
        } catch (Exception e) {
            throw new GameSaveException("Failed to save game data", e);
        }
    }
}
