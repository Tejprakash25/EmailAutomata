package com.emailautomata.feature.template;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.web.ApiResponse;
import com.emailautomata.core.web.ApiResponses;
import com.emailautomata.feature.template.dto.CreateTemplateRequest;
import com.emailautomata.feature.template.dto.TemplateResponse;
import com.emailautomata.feature.template.dto.TemplateSummaryResponse;
import com.emailautomata.feature.template.dto.UpdateTemplateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Template endpoints. Thin by design — the principal is resolved by Spring
 * Security, validation by {@code @Valid}, failures by the global handler.
 */
@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TemplateSummaryResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponses.ok(service.list(principal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> get(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        return ApiResponses.ok(service.get(principal, id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateTemplateRequest request) {
        TemplateResponse created = service.create(principal, request);
        return ApiResponses.created(created, URI.create("/api/v1/templates/" + created.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> update(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateRequest request) {
        return ApiResponses.ok(service.update(principal, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        service.delete(principal, id);
        return ApiResponses.noContent();
    }
}