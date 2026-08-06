package com.emailautomata.core.error;

import com.emailautomata.core.web.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the failure contract. These assert the shape clients depend
 * on, so a future refactor cannot silently change the envelope.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("A not-found business exception maps to 404 with its error code")
    void notFoundMapsTo404() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusiness(new ResourceNotFoundException("Template", 42L),
                        new MockHttpServletRequest("GET", "/api/v1/templates/42"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().error().code()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.name());
        assertThat(response.getBody().error().message()).contains("42");
    }

    @Test
    @DisplayName("A duplicate resource maps to 409 and names the offending field")
    void duplicateMapsTo409WithFieldDetail() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusiness(new DuplicateResourceException("User", "email", "a@b.com"),
                        new MockHttpServletRequest("POST", "/api/v1/users"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error().code()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE.name());
        assertThat(response.getBody().error().details()).containsKey("email");
    }

    @Test
    @DisplayName("An unexpected exception returns 500 with an incident id and no internals")
    void unexpectedReturnsIncidentIdOnly() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleUnexpected(new IllegalArgumentException("connection string user=root password=hunter2"),
                        new MockHttpServletRequest("GET", "/api/v1/meta"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error().code()).isEqualTo(ErrorCode.INTERNAL_ERROR.name());
        assertThat(response.getBody().error().details()).containsKey("incidentId");
        assertThat(response.getBody().error().message()).doesNotContain("password");
    }

    @Test
    @DisplayName("Every error code carries a status consistent with its name")
    void errorCodesHaveCoherentStatuses() {
        assertThat(ErrorCode.VALIDATION_FAILED.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.INVALID_CREDENTIALS.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.DUPLICATE_RESOURCE.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.INTERNAL_ERROR.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}