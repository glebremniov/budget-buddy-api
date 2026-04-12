package com.budget.buddy.budget_buddy_api.base;

import com.budget.buddy.budget_buddy_contracts.generated.model.FieldError;
import com.budget.buddy.budget_buddy_contracts.generated.model.Problem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;


@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  private static final String REQUEST_URI = "/test";
  private static final URI PROBLEM_TYPE = URI.create("about:blank");
  @Mock
  private ServletWebRequest webRequest;
  @Mock
  private HttpServletRequest httpServletRequest;
  private GlobalExceptionHandler handler;

  private static void assertProblemResponse(ResponseEntity<Problem> response) {
    assertThat(response)
        .extracting(HttpEntity::getHeaders)
        .returns(APPLICATION_PROBLEM_JSON, HttpHeaders::getContentType);

    assertThat(response)
        .extracting(HttpEntity::getBody)
        .returns(PROBLEM_TYPE, Problem::getType)
        .returns(URI.create(REQUEST_URI), Problem::getInstance);
  }

  @BeforeEach
  void init() {
    when(httpServletRequest.getRequestURI()).thenReturn(REQUEST_URI);
    when(webRequest.getRequest()).thenReturn(httpServletRequest);
    handler = new GlobalExceptionHandler();
  }

  static class MockPropertyPath implements Path {

    private final String name;

    MockPropertyPath(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public Iterator<Node> iterator() {
      return Collections.emptyIterator();
    }
  }

  @Nested
  @DisplayName("MethodArgumentNotValidException Handler")
  class MethodArgumentNotValidExceptionTests {

    private static void assertResponseEntity(ResponseEntity<Problem> response) {
      assertProblemResponse(response);
      assertThat(response)
          .returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode)
          .extracting(ResponseEntity::getBody)
          .returns(HttpStatus.BAD_REQUEST.value(), Problem::getStatus)
          .returns(HttpStatus.BAD_REQUEST.getReasonPhrase(), Problem::getTitle)
          .returns("One or more fields are invalid", Problem::getDetail)
          .returns(PROBLEM_TYPE, Problem::getType);

      assertThat(response.getBody())
          .extracting(Problem::getErrors)
          .isNotNull();
    }

    @Test
    @DisplayName("should handle validation exception with single field error")
    void shouldHandleSingleFieldError() {
      // Given
      var exception = mock(MethodArgumentNotValidException.class);
      var bindingResult = mock(org.springframework.validation.BindingResult.class);
      var fieldError = new org.springframework.validation.FieldError("object", "field", "message");
      when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
      when(exception.getBindingResult()).thenReturn(bindingResult);

      // When
      var response = handler.handleValidationException(exception, webRequest);

      // Then
      assertResponseEntity(response);
      var problem = response.getBody();
      assertThat(problem.getErrors())
          .hasSize(1)
          .containsExactly(new FieldError().field("field").message("message"));
    }
  }

  @Nested
  @DisplayName("ConstraintViolationException Handler")
  class ConstraintViolationExceptionTests {

    private static void assertResponseEntity(ResponseEntity<Problem> response) {
      assertProblemResponse(response);
      assertThat(response)
          .returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode)
          .extracting(ResponseEntity::getBody)
          .returns(HttpStatus.BAD_REQUEST.value(), Problem::getStatus)
          .returns(HttpStatus.BAD_REQUEST.getReasonPhrase(), Problem::getTitle)
          .returns("Constraint violations", Problem::getDetail);

      assertThat(response.getBody())
          .extracting(Problem::getErrors)
          .isNotNull();
    }

    @Test
    @DisplayName("should handle constraint violation with single violation")
    void shouldHandleSingleConstraintViolation() {
      // Given
      var violation = mock(ConstraintViolation.class);
      when(violation.getPropertyPath()).thenReturn(new MockPropertyPath("username"));
      when(violation.getMessage()).thenReturn("must be unique");

      var violations = new HashSet<ConstraintViolation<?>>();
      violations.add(violation);

      var exception = new ConstraintViolationException(violations);

      // When
      var response = handler.handleConstraintViolation(exception, webRequest);

      // Then
      assertResponseEntity(response);
      var problem = response.getBody();
      assertThat(problem.getErrors())
          .hasSize(1)
          .containsExactly(new FieldError().field("username").message("must be unique"));
    }

    @Test
    @DisplayName("should handle constraint violation with multiple violations")
    void shouldHandleMultipleConstraintViolations() {
      // Given
      var violation1 = mock(ConstraintViolation.class);
      when(violation1.getPropertyPath()).thenReturn(new MockPropertyPath("email"));
      when(violation1.getMessage()).thenReturn("invalid email format");

      var violation2 = mock(ConstraintViolation.class);
      when(violation2.getPropertyPath()).thenReturn(new MockPropertyPath("age"));
      when(violation2.getMessage()).thenReturn("must be at least 18");

      var violations = new HashSet<ConstraintViolation<?>>();
      violations.add(violation1);
      violations.add(violation2);

      var exception = new ConstraintViolationException(violations);

      // When
      var response = handler.handleConstraintViolation(exception, webRequest);

      // Then
      assertResponseEntity(response);
      var problem = response.getBody();
      assertThat(problem.getErrors())
          .hasSize(2)
          .contains(new FieldError().field("email").message("invalid email format"),
              new FieldError().field("age").message("must be at least 18"));
    }

    @Test
    @DisplayName("should handle constraint violation with null property path")
    void shouldHandleNullPropertyPath() {
      // Given
      var violation = mock(ConstraintViolation.class);
      when(violation.getPropertyPath()).thenReturn(null);
      when(violation.getMessage()).thenReturn("validation failed");

      var violations = new HashSet<ConstraintViolation<?>>();
      violations.add(violation);

      var exception = new ConstraintViolationException(violations);

      // When
      var response = handler.handleConstraintViolation(exception, webRequest);

      // Then
      assertResponseEntity(response);
      var problem = response.getBody();
      assertThat(problem.getErrors())
          .hasSize(1)
          .containsExactly(new FieldError().field("null").message("validation failed"));
    }
  }

  @Nested
  @DisplayName("NoSuchElementException Handler")
  class NoSuchElementExceptionTests {

    @ParameterizedTest
    @ValueSource(strings = {"Category not found", "User not found", "Transaction not found"})
    @DisplayName("should handle NoSuchElementException with various messages")
    void shouldHandleNotFound(String message) {
      // Given
      var exception = new NoSuchElementException(message);

      // When
      var response = handler.handleNotFound(exception, webRequest);

      // Then
      assertProblemResponse(response);
      assertThat(response)
          .returns(HttpStatus.NOT_FOUND, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns(HttpStatus.NOT_FOUND.getReasonPhrase(), Problem::getTitle);
    }
  }

  @Nested
  @DisplayName("NoResourceFoundException Handler")
  class NoResourceFoundExceptionTests {

    @Test
    @DisplayName("should handle NoResourceFoundException")
    void shouldHandleNoResourceFoundException() {
      // Given
      var exception = mock(NoResourceFoundException.class);
      when(exception.getMessage()).thenReturn("Resource /api/not-found not found");

      // When
      var response = handler.handleNoResourceFoundException(exception, webRequest);

      // Then
      assertProblemResponse(response);
      assertThat(response)
          .returns(HttpStatus.NOT_FOUND, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns(HttpStatus.NOT_FOUND.getReasonPhrase(), Problem::getTitle)
          .returns("Resource not found", Problem::getDetail)
          .returns(HttpStatus.NOT_FOUND.value(), Problem::getStatus);
    }
  }

  @Nested
  @DisplayName("AccessDeniedException Handler")
  class AccessDeniedExceptionTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "Access denied",
        "Insufficient permissions",
        "User does not have required role"
    })
    @DisplayName("should handle AccessDeniedException with various messages")
    void shouldHandleAccessDenied(String message) {
      // Given
      var exception = new AccessDeniedException(message);

      // When
      var response = handler.handleAccessDenied(exception, webRequest);

      // Then
      assertProblemResponse(response);
      assertThat(response)
          .returns(HttpStatus.FORBIDDEN, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns(HttpStatus.FORBIDDEN.getReasonPhrase(), Problem::getTitle)
          .returns("Access denied", Problem::getDetail)
          .returns(HttpStatus.FORBIDDEN.value(), Problem::getStatus);
    }

    @Nested
    @DisplayName("AuthenticationException Handler")
    class AuthenticationExceptionTests {

      @ParameterizedTest
      @ValueSource(strings = {
          "Invalid credentials",
          "Token expired",
          "Invalid or missing token"
      })
      @DisplayName("should handle AuthenticationException with various messages")
      void shouldHandleAuthenticationException(String message) {
        // Given
        var exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn(message);

        // When
        var response = handler.handleAuthenticationException(exception, webRequest);

        // Then
        assertProblemResponse(response);
        assertThat(response)
            .returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode)
            .extracting(HttpEntity::getBody)
            .returns(HttpStatus.UNAUTHORIZED.getReasonPhrase(), Problem::getTitle)
            .returns("Authentication failed", Problem::getDetail)
            .returns(HttpStatus.UNAUTHORIZED.value(), Problem::getStatus);
      }
    }
  }

  @Nested
  @DisplayName("DataIntegrityViolationException Handler")
  class DataIntegrityViolationExceptionTests {

    @Test
    @DisplayName("should handle DataIntegrityViolationException")
    void shouldHandleDataIntegrity() {
      // Given
      var cause = new RuntimeException("Unique constraint violation");
      var exception = new DataIntegrityViolationException(
          "Data integrity violation", cause);

      // When
      var response = handler.handleDataIntegrity(exception, webRequest);

      // Then
      assertThat(response.getStatusCode())
          .isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody())
          .isNotNull()
          .returns(409, Problem::getStatus);
    }
  }

  @Nested
  @DisplayName("HttpMessageNotReadableException Handler")
  class HttpMessageNotReadableExceptionTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "Malformed JSON",
        "Invalid request body",
        "Missing required field"
    })
    @DisplayName("should handle HttpMessageNotReadableException with various messages")
    void shouldHandleNotReadable(String message) {
      // Given
      var exception = mock(
          HttpMessageNotReadableException.class);
      when(exception.getMessage()).thenReturn(message);

      // When
      var response = handler.handleNotReadable(exception, webRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns(HttpStatus.BAD_REQUEST.getReasonPhrase(), Problem::getTitle)
          .returns(400, Problem::getStatus);
    }
  }

  @Nested
  @DisplayName("IllegalArgumentException Handler")
  class IllegalArgumentExceptionTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "Invalid argument",
        "Invalid ID format",
        "Page number must be positive"
    })
    @DisplayName("should handle IllegalArgumentException with various messages")
    void shouldHandleIllegalArgument(String message) {
      // Given
      var exception = new IllegalArgumentException(message);

      // When
      var response = handler.handleIllegalArgument(exception, webRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns(HttpStatus.BAD_REQUEST.getReasonPhrase(), Problem::getTitle)
          .returns(400, Problem::getStatus);
    }
  }

  @Nested
  @DisplayName("UnsupportedOperationException Handler")
  class UnsupportedOperationExceptionTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "Operation not supported",
        "Feature not implemented",
        "This operation is not allowed"
    })
    @DisplayName("should handle UnsupportedOperationException with various messages")
    void shouldHandleUnsupported(String message) {
      // Given
      var exception = new UnsupportedOperationException(message);

      // When
      var response = handler.handleUnsupported(exception, webRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), Problem::getTitle)
          .returns("Operation not supported", Problem::getDetail)
          .returns(501, Problem::getStatus);
    }
  }

  @Nested
  @DisplayName("Generic Exception Handler")
  class GenericExceptionTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "Unexpected error",
        "Something went wrong",
        "Database connection failed"
    })
    @DisplayName("should handle generic Exception")
    void shouldHandleGeneric(String message) {
      // Given
      var exception = new Exception(message);

      // When
      var response = handler.handleGeneric(exception, webRequest);

      // Then
      assertProblemResponse(response);
      assertThat(response)
          .returns(HttpStatus.INTERNAL_SERVER_ERROR, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), Problem::getTitle)
          .returns("An unexpected error occurred", Problem::getDetail)
          .returns(HttpStatus.INTERNAL_SERVER_ERROR.value(), Problem::getStatus);
    }
  }

}
