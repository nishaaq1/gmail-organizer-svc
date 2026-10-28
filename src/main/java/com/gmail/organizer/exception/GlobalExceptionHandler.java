package com.gmail.organizer.exception;

import com.gmail.organizer.dto.ApiError;
import com.gmail.organizer.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(
            AppException ex, HttpServletRequest request) {
        log.warn("Request failed code={} message={}", ex.code(), ex.getMessage());
        return ResponseEntity.status(ex.status()).body(
                ApiResponse.failure(requestUid(request),
                        new ApiError(ex.code(), ex.getMessage(), List.of())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(ApiResponse.failure(requestUid(request),
                new ApiError("VALIDATION_ERROR", "Request validation failed", details)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(requestUid(request),
                new ApiError("INVALID_ARGUMENT", ex.getMessage(), List.of())));
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(requestUid(request),
                new ApiError("BAD_REQUEST", "Request is invalid or malformed", List.of())));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleGoogleResponse(
            WebClientResponseException ex, HttpServletRequest request) {
        log.error("Upstream Gmail API returned status={}", ex.getStatusCode().value());
        HttpStatus status = ex.getStatusCode().value() == 401
                ? HttpStatus.UNAUTHORIZED
                : ex.getStatusCode().value() == 429
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : HttpStatus.BAD_GATEWAY;
        String code = status == HttpStatus.UNAUTHORIZED ? "GMAIL_AUTH_FAILED"
                : status == HttpStatus.TOO_MANY_REQUESTS ? "GMAIL_RATE_LIMITED" : "GMAIL_API_ERROR";
        return ResponseEntity.status(status).body(ApiResponse.failure(requestUid(request),
                new ApiError(code, "Gmail API request failed", List.of())));
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleNetwork(
            WebClientRequestException ex, HttpServletRequest request) {
        log.error("Unable to reach Gmail API: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                ApiResponse.failure(requestUid(request),
                        new ApiError("GMAIL_UNAVAILABLE", "Unable to reach Gmail API", List.of())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected application error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(requestUid(request),
                        new ApiError("INTERNAL_ERROR", "An unexpected error occurred", List.of())));
    }

    private String requestUid(HttpServletRequest request) {
        Object id = request.getAttribute(CorrelationIdFilter.REQUEST_UID_ATTRIBUTE);
        return id == null ? "unknown" : id.toString();
    }
}
