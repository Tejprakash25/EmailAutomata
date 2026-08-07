package com.emailautomata.feature.recipient;

import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.core.web.ApiResponse;
import com.emailautomata.core.web.ApiResponses;
import com.emailautomata.feature.recipient.dto.CreateListRequest;
import com.emailautomata.feature.recipient.dto.CreateRecipientRequest;
import com.emailautomata.feature.recipient.dto.CsvImportRequest;
import com.emailautomata.feature.recipient.dto.CsvImportResult;
import com.emailautomata.feature.recipient.dto.RecipientListResponse;
import com.emailautomata.feature.recipient.dto.RecipientResponse;
import com.emailautomata.feature.recipient.dto.UpdateRecipientRequest;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/recipients")
public class RecipientController {

    private final RecipientService service;

    public RecipientController(RecipientService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RecipientResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PageableDefault(size = 25, sort = "createdAt") Pageable pageable) {
        return ApiResponses.ok(service.list(principal, pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecipientResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateRecipientRequest request) {
        RecipientResponse created = service.create(principal, request);
        return ApiResponses.created(created, URI.create("/api/v1/recipients/" + created.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipientResponse>> update(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecipientRequest request) {
        return ApiResponses.ok(service.update(principal, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        service.delete(principal, id);
        return ApiResponses.noContent();
    }

    // ------------------------------------------------------------------ lists

    @GetMapping("/lists")
    public ResponseEntity<ApiResponse<List<RecipientListResponse>>> lists(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ApiResponses.ok(service.allLists(principal));
    }

    @PostMapping("/lists")
    public ResponseEntity<ApiResponse<RecipientListResponse>> createList(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateListRequest request) {
        RecipientListResponse created = service.createList(principal, request);
        return ApiResponses.created(created, URI.create("/api/v1/recipients/lists/" + created.id()));
    }

    @DeleteMapping("/lists/{id}")
    public ResponseEntity<Void> deleteList(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        service.deleteList(principal, id);
        return ApiResponses.noContent();
    }

    // ----------------------------------------------------------------- import

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<CsvImportResult>> importCsv(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CsvImportRequest request) {
        return ApiResponses.ok(service.importCsv(principal, request));
    }
}