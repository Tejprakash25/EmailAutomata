package com.emailautomata.feature.analytics;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.web.ApiResponse;
import com.emailautomata.core.web.ApiResponses;
import com.emailautomata.feature.analytics.dto.DashboardStats;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> stats(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponses.ok(statsService.forUser(principal));
    }
}