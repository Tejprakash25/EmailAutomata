package com.emailautomata.feature.identity;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.web.ApiResponse;
import com.emailautomata.core.web.ApiResponses;
import com.emailautomata.feature.identity.dto.AuthResponse;
import com.emailautomata.feature.identity.dto.LoginRequest;
import com.emailautomata.feature.identity.dto.RegisterRequest;
import com.emailautomata.feature.identity.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Authentication endpoints.
 *
 * <p>Thin by design: bind, validate, delegate, wrap. No try/catch — the global
 * handler owns failure translation.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponses.created(response, URI.create("/api/v1/auth/me"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponses.ok(authService.login(request));
    }

    /**
     * Resolves the caller from their token. The client calls this on boot to
     * restore a session without holding profile data in storage.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponses.ok(authService.currentProfile(principal));
    }
}