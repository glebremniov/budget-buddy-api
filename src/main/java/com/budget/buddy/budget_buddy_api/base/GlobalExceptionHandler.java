package com.budget.buddy.budget_buddy_api.base;

import com.budget.buddy.budget_buddy_api.generated.model.Problem;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the application. Converts various exceptions into standardized {@link Problem} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<Problem> problemResponse(HttpStatus status, String title, String detail, WebRequest request) {
    var problem = new Problem()
        .type(URI.create("about:blank"))
        .title(title)
        .status(status.value())
        .detail(detail)
        .instance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    return new ResponseEntity<>(problem, headers, status);
  }

  /**
   * Handles validation exceptions when method arguments are not valid.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
    return problemResponse(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid", request);
  }

  /**
   * Handles constraint violation exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
    return problemResponse(HttpStatus.BAD_REQUEST, "Validation failed", "Constraint violations", request);
  }

  /**
   * Handles cases where a requested resource is not found.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Problem> handleNotFound(NoSuchElementException ex, WebRequest request) {
    log.debug("Entity not found: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), request);
  }

  /**
   * Handles cases where a requested resource is not found.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Problem> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
    log.debug("Resource not found: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), request);
  }

  /**
   * Handles access denied exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    log.warn("Access denied: {}", ex.getMessage());
    return problemResponse(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage(), request);
  }

  /**
   * Handles authentication exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Problem> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return problemResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", ex.getMessage(), request);
  }

  /**
   * Handles data integrity violation exceptions (e.g., unique constraint violations).
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Problem> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    return problemResponse(HttpStatus.CONFLICT, "Data integrity violation", ex.getMostSpecificCause().getMessage(), request);
  }

  /**
   * Handles cases where the HTTP message is not readable (e.g., malformed JSON).
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Problem> handleNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
    log.debug("Malformed request: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, "Malformed request", ex.getMessage(), request);
  }

  /**
   * Handles illegal argument exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Problem> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
    log.debug("Illegal argument: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, "Invalid argument", ex.getMessage(), request);
  }

  /**
   * Handles all other unhandled exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleGeneric(Exception ex, WebRequest request) {
    log.error("Unhandled exception: {}", ex.getMessage(), ex);
    return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred", request);
  }

  /**
   * Handles unsupported operation exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<Problem> handleUnsupported(UnsupportedOperationException ex, WebRequest request) {
    log.warn("Unsupported operation: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_IMPLEMENTED, "Not implemented", ex.getMessage(), request);
  }
}
