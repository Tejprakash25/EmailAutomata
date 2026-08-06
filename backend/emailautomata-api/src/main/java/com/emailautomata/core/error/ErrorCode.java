    package com.emailautomata.core.error;

    import org.springframework.http.HttpStatus;

    /**
     * Every failure the API can return, in one enumerable place.
     *
     * <p>Codes are part of the public API contract: the client branches on them,
     * so they are stable even when the accompanying message is reworded. Keeping
     * the HTTP status alongside the code prevents the same logical failure being
     * returned as a 400 by one endpoint and a 409 by another.</p>
     */
    public enum ErrorCode {

        VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "One or more fields are invalid."),
        MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "The request body could not be read."),
        MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "A required parameter is missing."),
        TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "A parameter has the wrong type."),

        UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
        INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."),
        ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have access to this resource."),

        RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource does not exist."),
        ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "No endpoint matches this request."),
        METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "That method is not supported here."),

        DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "A resource with those details already exists."),
        ILLEGAL_STATE_TRANSITION(HttpStatus.CONFLICT, "That operation is not valid in the current state."),
        DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "The operation conflicts with existing data."),

        PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "The uploaded content is too large."),

        INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our end.");

        private final HttpStatus status;
        private final String defaultMessage;

        ErrorCode(HttpStatus status, String defaultMessage) {
            this.status = status;
            this.defaultMessage = defaultMessage;
        }

        public HttpStatus status() {
            return status;
        }

        public String defaultMessage() {
            return defaultMessage;
        }
    }