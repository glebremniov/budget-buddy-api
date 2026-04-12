# Agent Conventions

## Error Handling

- **RFC 9457 (Problem Details)**: All error responses must follow the Problem Details for HTTP APIs specification.
- **Standardized Titles**: When `type` is `about:blank`, the `title` SHOULD be the same as the HTTP status phrase (e.g., "Bad Request" for 400).
- **Field-Level Errors**: Validation exceptions (`MethodArgumentNotValidException`, `ConstraintViolationException`) must return a `Problem` containing an `errors` array with `field` and `message` properties.
- **Request URI**: The `instance` field should contain the current request URI, retrieved using `ServletWebRequest` in `GlobalExceptionHandler`.

## Code Patterns

- **Problem Details Extension**: The base `Problem` class from `budget-buddy-contracts` now includes an `errors` field for field-level validation errors. Use it directly instead of extending it for common validation cases.
- **Validation Handling**:
  - Prefer using `ex.getBindingResult().getFieldErrors()` for `MethodArgumentNotValidException`.
  - Prefer using `ex.getConstraintViolations()` for `ConstraintViolationException`.
