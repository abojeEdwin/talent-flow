package com.talentFlow.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exception, HttpServletRequest request) {
        log.warn(
                "API exception at path {} with status {}: {}",
                request.getRequestURI(),
                exception.getStatus().value(),
                exception.getMessage()
        );
        return ResponseEntity.status(exception.getStatus()).body(buildBody(exception.getStatus(), exception.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed at path {} with {} field errors", request.getRequestURI(), errors.size());
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed", request);
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        log.warn("Constraint violation at path {}: {}", request.getRequestURI(), exception.getMessage());
        return ResponseEntity.badRequest().body(buildBody(HttpStatus.BAD_REQUEST, exception.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                                  HttpServletRequest request) {
        String parameterName = exception.getName();
        String message = "Invalid value for parameter '" + parameterName + "'";

        if ("courseId".equals(parameterName)) {
            message = "Invalid courseId format";
        }

        log.warn("Type mismatch at path {} for parameter {}: {}", request.getRequestURI(), parameterName, exception.getValue());
        return ResponseEntity.badRequest().body(buildBody(HttpStatus.BAD_REQUEST, message, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception for path {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request));
    }

    private Map<String, Object> buildBody(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return body;
    }
}
