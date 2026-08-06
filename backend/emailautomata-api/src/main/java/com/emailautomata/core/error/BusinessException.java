package com.emailautomata.core.error;

import java.util.Map;

/**
 * Base type for failures that are expected outcomes of business rules rather
 * than defects.
 *
 * <p>Services throw these; {@link GlobalExceptionHandler} translates them.
 * A service never constructs an HTTP response, and a controller never writes a
 * try/catch — which is what keeps controllers thin and the failure contract
 * uniform.</p>
 *
 * <p>Stack traces are suppressed: these are control flow, not defects, and
 * filling in a trace for every not-found is measurable overhead on a hot
 * path.</p>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Map<String, String> details;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), null);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, String> details) {
        super(message, null, false, false);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}