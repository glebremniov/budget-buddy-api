package com.budget.buddy.budget_buddy_api.base;

import com.budget.buddy.budget_buddy_api.generated.model.Problem;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<Problem> problemResponse(HttpStatus status, String title, String detail) {
    var problem = new Problem()
        .type("about:blank")
        .title(title)
        .status(status.value())
        .detail(detail);

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    return new ResponseEntity<>(problem, headers, status);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
    return problemResponse(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid");
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException ex) {
    return problemResponse(HttpStatus.BAD_REQUEST, "Validation failed", "Constraint violations");
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Problem> handleNotFound(NoSuchElementException ex) {
    log.debug("Entity not found: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Problem> handleNoResourceFoundException(NoResourceFoundException ex) {
    log.debug("Resource not found: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return problemResponse(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Problem> handleAuthenticationException(AuthenticationException ex) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return problemResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Problem> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    return problemResponse(HttpStatus.CONFLICT, "Data integrity violation", ex.getMostSpecificCause().getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Problem> handleNotReadable(HttpMessageNotReadableException ex) {
    log.debug("Malformed request: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, "Malformed request", ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Problem> handleIllegalArgument(IllegalArgumentException ex) {
    log.debug("Illegal argument: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, "Invalid argument", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleGeneric(Exception ex) {
    log.error("Unhandled exception: {}", ex.getMessage(), ex);
    return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred");
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<Problem> handleUnsupported(UnsupportedOperationException ex) {
    log.warn("Unsupported operation: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_IMPLEMENTED, "Not implemented", ex.getMessage());
  }
}
