package com.acme.rockpaperscissors.controller;

import com.acme.rockpaperscissors.enums.GameResult;
import com.acme.rockpaperscissors.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "Observe game statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics observed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/observe")
    public ResponseEntity<String> observeStatistics(@RequestParam @NotNull String username) {
        String response = statisticsService.observeStatistics(username);
        return ResponseEntity.ok(response);
    }
}
