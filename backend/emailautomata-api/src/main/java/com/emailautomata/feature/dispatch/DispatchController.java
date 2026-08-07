package com.emailautomata.feature.dispatch;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.web.ApiResponse;
import com.emailautomata.core.web.ApiResponses;
import com.emailautomata.feature.dispatch.ComposeService.ComposeResult;
import com.emailautomata.feature.dispatch.dto.ComposeRequest;
import com.emailautomata.feature.dispatch.dto.DispatchResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/dispatches")
public class DispatchController {

    private final ComposeService composeService;
    private final SendService sendService;

    public DispatchController(ComposeService composeService, SendService sendService) {
        this.composeService = composeService;
        this.sendService = sendService;
    }

    /**
     * Composes a draft dispatch. Returns 201 with the draft and its readiness
     * preview, so the client can immediately warn about unresolved recipients.
     */
    @PostMapping("/compose")
    public ResponseEntity<ApiResponse<ComposeResult>> compose(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ComposeRequest request) {
        ComposeResult result = composeService.compose(principal, request);
        return ApiResponses.created(result,
                URI.create("/api/v1/dispatches/" + result.dispatch().id()));
    }

    /** Sends a draft immediately, returning the per-recipient tally. */
    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<SendResult>> send(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        return ApiResponses.ok(sendService.sendNow(principal, id));
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