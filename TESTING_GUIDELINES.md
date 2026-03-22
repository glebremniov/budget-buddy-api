# Testing Guidelines

This document outlines the requirements and best practices for writing tests in the Budget Buddy API project.

## 1. Local Variables
Always use the `var` keyword for local variable declarations to improve readability and reduce verbosity.

```java
// Good
var entity = new TransactionEntity();
var result = service.create(request);

// Avoid
TransactionEntity entity = new TransactionEntity();
Transaction result = service.create(request);
```

## 2. Test Structure
Tests must follow the **Given-When-Then** pattern, clearly separated by comments.

```java
@Test
void should_DoSomething() {
    // Given
    var input = "test";
    when(repository.find(input)).thenReturn(Optional.empty());

    // When
    var result = service.process(input);

    // Then
    assertThat(result).isNotNull();
}
```

## 3. Mockito Matchers
Avoid using vague matchers like `any()` or `eq()` whenever possible.
- Use exact values if they are known.
- Use `ArgumentCaptor` to verify the state of objects passed to mocked methods.

```java
// Good: Using ArgumentCaptor for verification
var captor = ArgumentCaptor.forClass(TransactionFilter.class);
verify(repository).findAllByFilter(captor.capture(), any());
assertThat(captor.getValue().ownerId()).isEqualTo(currentUserId);

// Avoid: Vague matchers without state verification
verify(repository).findAllByFilter(any(), any());
```

## 4. Assertions (AssertJ)
### Multi-field Assertions
Use `returns()` to verify multiple properties of an object in a single assertion chain instead of multiple `isEqualTo()` calls.

```java
// Good
assertThat(result)
    .returns(expectedName, Category::getName)
    .returns(expectedIcon, Category::getIcon)
    .returns(expectedColor, Category::getColor);

// Avoid
assertThat(result.getName()).isEqualTo(expectedName);
assertThat(result.getIcon()).isEqualTo(expectedIcon);
assertThat(result.getColor()).isEqualTo(expectedColor);
```

### Descriptive Assertions
Use `as()` to provide meaningful descriptions for assertions. This helps identify the cause of failure quickly from test logs.

```java
assertThat(result.getId())
    .as("Generated ID should not be null for new entities")
    .isNotNull();
```

## 5. Naming Conventions
- Test classes should be named after the class they test, suffixed with `Test`.
- Use descriptive method names, preferably starting with `should_`.
- Use `@Nested` classes to group related tests (e.g., by method or scenario).
