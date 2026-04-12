package com.budget.buddy.budget_buddy_api.base.validation;

import com.budget.buddy.budget_buddy_contracts.generated.model.FieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FieldErrorFactory Tests")
class FieldErrorFactoryTest {

  @Nested
  @DisplayName("from(ConstraintViolation)")
  class ConstraintViolationTests {

    @Mock
    private ConstraintViolation<?> violation;

    @Mock
    private Path propertyPath;

    @Test
    @DisplayName("should map ConstraintViolation with property path to FieldError")
    void shouldMapWithPropertyPath() {
      // Given
      when(violation.getPropertyPath()).thenReturn(propertyPath);
      when(propertyPath.toString()).thenReturn("username");
      when(violation.getMessage()).thenReturn("must not be blank");

      // When
      FieldError result = FieldErrorFactory.from(violation);

      // Then
      assertThat(result)
          .returns("username", FieldError::getField)
          .returns("must not be blank", FieldError::getMessage);
    }

    @Test
    @DisplayName("should map ConstraintViolation with null property path to 'null' field")
    void shouldMapWithNullPropertyPath() {
      // Given
      when(violation.getPropertyPath()).thenReturn(null);
      when(violation.getMessage()).thenReturn("invalid");

      // When
      FieldError result = FieldErrorFactory.from(violation);

      // Then
      assertThat(result)
          .returns("null", FieldError::getField)
          .returns("invalid", FieldError::getMessage);
    }

    @Test
    @DisplayName("should throw NullPointerException when violation is null")
    void shouldThrowNpe() {
      assertThatThrownBy(() -> FieldErrorFactory.from((ConstraintViolation<?>) null))
          .isExactlyInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("from(org.springframework.validation.FieldError)")
  class SpringFieldErrorTests {

    @Test
    @DisplayName("should map Spring FieldError to contract FieldError")
    void shouldMapSpringFieldError() {
      // Given
      var springFieldError = new org.springframework.validation.FieldError(
          "user", "email", "invalid format"
      );

      // When
      FieldError result = FieldErrorFactory.from(springFieldError);

      // Then
      assertThat(result)
          .returns("email", FieldError::getField)
          .returns("invalid format", FieldError::getMessage);
    }

    @Test
    @DisplayName("should throw NullPointerException when fieldError is null")
    void shouldThrowNpe() {
      assertThatThrownBy(() -> FieldErrorFactory.from((org.springframework.validation.FieldError) null))
          .isExactlyInstanceOf(NullPointerException.class);
    }
  }
}
