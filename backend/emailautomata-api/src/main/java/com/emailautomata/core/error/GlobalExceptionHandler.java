package com.emailautomata.core.error;

import com.emailautomata.core.web.ApiError;
import com.emailautomata.core.web.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Translates every exception the application can raise into the uniform
 * {@link ApiResponse} failure envelope.
 *
 * <p>This is the only place in the codebase that decides an HTTP status for a
 * failure. Controllers contain no try/catch, and services throw domain
 * exceptions without knowing anything about HTTP.</p>
 *
 * <p>Handlers are ordered from most specific to least. The catch-all logs the
 * full stack trace against a correlation id and returns only that id, so an
 * operator can locate the incident in the logs while the client learns nothing
 * about the internals.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------- domain

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex,
                                                            HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();

        // 5xx business failures are defects; 4xx are expected outcomes.
        if (code.status().is5xxServerError()) {
            log.error("Business failure [{}] on {} {}", code, request.getMethod(),
                    request.getRequestURI(), ex);
        } else {
            log.debug("Business failure [{}] on {} {}: {}", code, request.getMethod(),
                    request.getRequestURI(), ex.getMessage());
        }

        return respond(code, ex.getMessage(), ex.getDetails());
    }

    // ------------------------------------------------------------ validation

    /**
     * Bean Validation failure on an {@code @Valid @RequestBody} DTO.
     * Field errors are flattened to a field-to-message map the client can bind
     * straight onto inputs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // First message wins: a field with several violations reports the
            // first rather than an unstable concatenation.
            details.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() == null ? "is invalid" : fieldError.getDefaultMessage()
            );
        }

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                details.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        return respond(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage(), details);
    }

    /** Bean Validation failure on a method parameter (path variable, request param). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> details = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            details.putIfAbsent(field, violation.getMessage());
        });

        return respond(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage(), details);
    }

    // --------------------------------------------------------- malformed I/O

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        // The underlying parser message can leak class names, so it is logged
        // rather than returned.
        log.debug("Unreadable request body: {}", ex.getMessage());
        return respond(ErrorCode.MALFORMED_REQUEST, ErrorCode.MALFORMED_REQUEST.defaultMessage(), null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return respond(ErrorCode.MISSING_PARAMETER,
                "Required parameter '%s' is missing.".formatted(ex.getParameterName()),
                Map.of(ex.getParameterName(), "is required"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return respond(ErrorCode.TYPE_MISMATCH,
                "Parameter '%s' has an invalid value.".formatted(ex.getName()),
                Map.of(ex.getName(), "is not a valid value"));
    }

    // ------------------------------------------------------------- routing

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return respond(ErrorCode.ENDPOINT_NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND.defaultMessage(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED,
                "%s is not supported for this endpoint.".formatted(ex.getMethod()), null);
    }

    // ------------------------------------------------------------ persistence

    /**
     * Database constraint violation that slipped past service-level checks —
     * typically a race between two concurrent inserts.
     *
     * <p>The driver message is never returned: it exposes table, column and
     * index names.</p>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                 HttpServletRequest request) {
        log.warn("Data integrity violation on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respond(ErrorCode.DATA_INTEGRITY_VIOLATION,
                ErrorCode.DATA_INTEGRITY_VIOLATION.defaultMessage(), null);
    }

    // -------------------------------------------------------------- catch-all

    /**
     * Anything unanticipated. The stack trace goes to the log against a
     * correlation id; the client receives the id and nothing else.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        String incidentId = UUID.randomUUID().toString().substring(0, 8);

        log.error("Unhandled exception [incident {}] on {} {}", incidentId,
                request.getMethod(), request.getRequestURI(), ex);

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.name(),
                ErrorCode.INTERNAL_ERROR.defaultMessage(),
                Map.of("incidentId", incidentId)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(error));
    }

    // ---------------------------------------------------------------- helper

    private ResponseEntity<ApiResponse<Void>> respond(ErrorCode code,
                                                      String message,
                                                      Map<String, String> details) {
        ApiError error = new ApiError(code.name(), message, details);
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(error));
    }
}