Run the appropriate test suite and surface failures clearly. Accepts an optional scope argument.

**Usage:**
- `/run-tests` — run unit tests only (`./gradlew test`)
- `/run-tests integration` — run integration tests only (`./gradlew integrationTest`)
- `/run-tests all` — run full check (`./gradlew check`)
- `/run-tests <ClassName>` — run a single test class (auto-detects unit vs integration by `IntegrationTest` suffix)

**Steps:**

1. Parse the argument:
   - No argument → unit tests: `./gradlew test`
   - `integration` → `./gradlew integrationTest`
   - `all` → `./gradlew check`
   - Class name ending in `IntegrationTest` → `./gradlew integrationTest --tests "com.budget.buddy.budget_buddy_api.<ClassName>"`
   - Any other class name → `./gradlew test --tests "com.budget.buddy.budget_buddy_api.<ClassName>"`

2. Run the command and capture output.

3. Parse the results:
   - If all tests pass: report the count of tests run and confirm success.
   - If tests fail: for each failure, show:
     - Test class and method name
     - The failure message / assertion error
     - The relevant stack frame (first non-framework line)
   - If compilation fails: show the compiler error directly — do not proceed to test analysis.

4. If there are failures, suggest the most likely fix based on the error type:
   - `AssertionError` → show expected vs actual and point to the assertion line
   - `NullPointerException` → identify what was null and where
   - `BeanCreationException` / context load failure → identify the misconfigured bean
   - SQL / migration error → identify the Liquibase changeset or query

5. Do **not** automatically fix failures unless the user asks. Just report clearly.
