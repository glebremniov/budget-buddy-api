package com.budget.buddy.budget_buddy_api.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import com.budget.buddy.budget_buddy_api.generated.model.Problem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  private static void assertContentType(ResponseEntity<Problem> response) {
    assertThat(response)
        .extracting(HttpEntity::getHeaders)
        .returns(APPLICATION_PROBLEM_JSON, HttpHeaders::getContentType);
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

    private static MapAssert<Object, Object> assertResponseEntity(ResponseEntity<Problem> response) {
      assertContentType(response);
      return assertThat(response)
          .returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode)
          .extracting(ResponseEntity::getBody)
          .returns(HttpStatus.BAD_REQUEST.value(), Problem::getStatus)
          .returns("Validation failed", Problem::getTitle)
          .returns("One or more fields are invalid", Problem::getDetail)
          .returns("about:blank", Problem::getType)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP);
    }

    @Test
    @DisplayName("should handle validation exception with single field error")
    void shouldHandleSingleFieldError() {
      // Given
      var fieldError = new FieldError("object", "email", "must be a valid email");
      var bindingResult = mock(BindingResult.class);
      when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

      var exception = mock(MethodArgumentNotValidException.class);
      when(exception.getBindingResult()).thenReturn(bindingResult);

      // When
      var response = handler.handleValidationException(exception, null);

      // Then
      assertResponseEntity(response)
          .containsEntry("email", List.of("must be a valid email"));
    }

    @Test
    @DisplayName("should handle validation exception with multiple field errors on same field")
    void shouldHandleMultipleErrorsSameField() {
      // Given
      var error1 = new FieldError("object", "password", "must not be empty");
      var error2 = new FieldError("object", "password", "must be at least 8 characters");
      var bindingResult = mock(BindingResult.class);
      when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

      var exception = mock(MethodArgumentNotValidException.class);
      when(exception.getBindingResult()).thenReturn(bindingResult);

      // When
      var response = handler.handleValidationException(exception, null);

      // Then
      assertResponseEntity(response)
          .containsEntry("password", List.of("must not be empty", "must be at least 8 characters"));
    }

    @Test
    @DisplayName("should handle validation exception with multiple fields")
    void shouldHandleMultipleFields() {
      // Given
      var error1 = new FieldError("object", "email", "must be a valid email");
      var error2 = new FieldError("object", "name", "must not be blank");
      var error3 = new FieldError("object", "age", "must be greater than or equal to 18");
      var bindingResult = mock(BindingResult.class);
      when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2, error3));

      var exception = mock(MethodArgumentNotValidException.class);
      when(exception.getBindingResult()).thenReturn(bindingResult);

      // When
      var response = handler.handleValidationException(exception, null);

      // Then
      assertResponseEntity(response)
          .containsEntry("email", List.of("must be a valid email"))
          .containsEntry("name", List.of("must not be blank"))
          .containsEntry("age", List.of("must be greater than or equal to 18"));
    }
  }

  @Nested
  @DisplayName("ConstraintViolationException Handler")
  class ConstraintViolationExceptionTests {

    private static MapAssert<Object, Object> assertResponseEntity(ResponseEntity<Problem> response) {
      assertContentType(response);
      return assertThat(response)
          .returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode)
          .extracting(ResponseEntity::getBody)
          .returns(HttpStatus.BAD_REQUEST.value(), Problem::getStatus)
          .returns("Validation failed", Problem::getTitle)
          .returns("Constraint violations", Problem::getDetail)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP);
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
      var response = handler.handleConstraintViolation(exception);

      // Then
      assertResponseEntity(response)
          .containsEntry("username", List.of("must be unique"));
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
      var response = handler.handleConstraintViolation(exception);

      // Then
      assertResponseEntity(response)
          .containsEntry("email", List.of("invalid email format"))
          .containsEntry("age", List.of("must be at least 18"));
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
      var response = handler.handleConstraintViolation(exception);

      // Then
      assertResponseEntity(response)
          .containsEntry("", List.of("validation failed"));
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
      var response = handler.handleNotFound(exception);

      // Then
      assertContentType(response);
      assertThat(response)
          .returns(HttpStatus.NOT_FOUND, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns("Resource not found", Problem::getTitle)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .isEmpty();
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
      var response = handler.handleNoResourceFoundException(exception);

      // Then
      assertContentType(response);
      assertThat(response)
          .returns(HttpStatus.NOT_FOUND, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns("Resource not found", Problem::getTitle)
          .returns("Resource /api/not-found not found", Problem::getDetail)
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
      var response = handler.handleAccessDenied(exception);

      // Then
      assertContentType(response);
      assertThat(response)
          .returns(HttpStatus.FORBIDDEN, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns("Access denied", Problem::getTitle)
          .returns(message, Problem::getDetail)
          .returns(HttpStatus.FORBIDDEN.value(), Problem::getStatus)
          .extracting(Problem::getErrors)
          .isEqualTo(Collections.emptyMap());
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
        var response = handler.handleAuthenticationException(exception);

        // Then
        assertContentType(response);
        assertThat(response)
            .returns(HttpStatus.UNAUTHORIZED, ResponseEntity::getStatusCode)
            .extracting(HttpEntity::getBody)
            .returns("Authentication failed", Problem::getTitle)
            .returns(message, Problem::getDetail)
            .returns(HttpStatus.UNAUTHORIZED.value(), Problem::getStatus)
            .extracting(Problem::getErrors)
            .isEqualTo(Collections.emptyMap());
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
      var response = handler.handleDataIntegrity(exception);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns(409, Problem::getStatus)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .isEmpty();
      assertThat(problem.getErrors()).isEmpty();
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
      var response = handler.handleNotReadable(exception);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns("Malformed request", Problem::getTitle)
          .returns(400, Problem::getStatus)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .isEmpty();
      assertThat(problem.getErrors()).isEmpty();
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
      var response = handler.handleIllegalArgument(exception);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns("Invalid argument", Problem::getTitle)
          .returns(400, Problem::getStatus)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .isEmpty();
      assertThat(problem.getErrors()).isEmpty();
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
      var response = handler.handleUnsupported(exception);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
      assertThat(response.getBody()).isNotNull();
      var problem = response.getBody();
      assertThat(problem)
          .returns("Not implemented", Problem::getTitle)
          .returns(message, Problem::getDetail)
          .returns(501, Problem::getStatus)
          .extracting(Problem::getErrors)
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .isEmpty();
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
      var response = handler.handleGeneric(exception);

      // Then
      assertContentType(response);
      assertThat(response)
          .returns(HttpStatus.INTERNAL_SERVER_ERROR, ResponseEntity::getStatusCode)
          .extracting(HttpEntity::getBody)
          .returns("Internal server error", Problem::getTitle)
          .returns("An unexpected error occurred", Problem::getDetail)
          .returns(HttpStatus.INTERNAL_SERVER_ERROR.value(), Problem::getStatus)
          .extracting(Problem::getErrors)
          .isEqualTo(Collections.emptyMap());
    }
  }

}
