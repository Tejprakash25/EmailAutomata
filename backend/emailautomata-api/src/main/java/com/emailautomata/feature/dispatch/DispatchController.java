package com.emailautomata.feature.dispatch;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.web.ApiResponse;
import com.emailautomata.core.web.ApiResponses;
import com.emailautomata.feature.dispatch.ComposeService.ComposeResult;
import com.emailautomata.feature.dispatch.dto.ComposeRequest;
import com.emailautomata.feature.dispatch.dto.DispatchHistoryRow;
import com.emailautomata.feature.dispatch.dto.DispatchResponse;
import com.emailautomata.feature.dispatch.dto.ScheduleRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/dispatches")
public class DispatchController {

    private final ComposeService composeService;
    private final SendService sendService;
    private final ScheduleService scheduleService;
    private final HistoryService historyService;

    public DispatchController(ComposeService composeService,
                              SendService sendService,
                              ScheduleService scheduleService,
                              HistoryService historyService) {
        this.composeService = composeService;
        this.sendService = sendService;
        this.scheduleService = scheduleService;
        this.historyService = historyService;
    }

    @PostMapping("/compose")
    public ResponseEntity<ApiResponse<ComposeResult>> compose(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ComposeRequest request) {
        ComposeResult result = composeService.compose(principal, request);
        return ApiResponses.created(result,
                URI.create("/api/v1/dispatches/" + result.dispatch().id()));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<SendResult>> send(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        return ApiResponses.ok(sendService.sendNow(principal, id));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<DispatchResponse>> schedule(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {
        return ApiResponses.ok(scheduleService.schedule(principal, id, request));
    }

    @PostMapping("/{id}/cancel-schedule")
    public ResponseEntity<ApiResponse<DispatchResponse>> cancelSchedule(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        return ApiResponses.ok(scheduleService.cancel(principal, id));
    }

    /**
     * Paginated, filterable sent history. status and search are optional and
     * compose: any combination works.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<DispatchHistoryRow>>> history(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
        return ApiResponses.ok(historyService.history(principal, status, search, pageable));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DispatchResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponses.ok(composeService.list(principal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DispatchResponse>> get(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        return ApiResponses.ok(composeService.get(principal, id));
    }
}