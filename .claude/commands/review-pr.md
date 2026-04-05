Review a pull request and post a structured GitHub review. Accepts a PR number or GitHub PR URL as the argument.

**Usage:** `/review-pr <pr-number-or-url>`

**Steps:**

1. Parse the PR number from the argument (extract digits if a URL was passed)

2. Fetch PR metadata and diff:
   ```
   gh pr view <number> --repo glebremniov/budget-buddy-api
   gh pr diff <number> --repo glebremniov/budget-buddy-api
   ```

3. Read the relevant changed source files from the local working tree to get full context (don't rely solely on the diff)

4. Review the changes against the following criteria:

   **Correctness**
   - Logic errors or off-by-one mistakes
   - Null/empty handling gaps
   - Incorrect use of Spring Data JDBC, MapStruct, or Liquibase patterns

   **Architecture & Conventions** (per CLAUDE.md)
   - API-first: spec changes should precede implementation
   - New domain features should follow: Entity → Repository → Mapper → Service → Controller
   - Controllers must extend `BaseEntityController` and delegate to `*Internal()` methods
   - Services must extend `OwnableEntityService` (enforces `ownerId` isolation)
   - Validators must implement `BaseEntityValidator`
   - Exceptions thrown from services, not caught in controllers
   - `var` used for all local variable declarations
   - Version bumped in `gradle.properties` if source/build/deployment files changed

   **Security**
   - No public endpoints added unintentionally (only `POST /v1/auth/**` and `GET /actuator/health` are public)
   - No sensitive data logged or exposed in responses
   - `ownerId` isolation not bypassed

   **Tests**
   - Integration tests in `src/integrationTest/` for new endpoints or service logic
   - Unit tests follow Given/When/Then structure
   - `ArgumentCaptor` used instead of vague `any()` matchers
   - Test methods named `should_<action>_When_<condition>()`

   **Misc**
   - No unnecessary abstractions or premature generalisation
   - No error handling for impossible scenarios
   - Liquibase migration added if schema changes are needed

5. Write the review — be direct and concrete. Group findings by severity:
   - **Must fix** — bugs, security issues, missing migrations, broken conventions
   - **Should fix** — test gaps, style violations, missing version bump
   - **Nit** — minor style, naming, or readability suggestions (prefix with "Nit:")

6. Post the review using `gh pr review`:
   ```
   gh pr review <number> --repo glebremniov/budget-buddy-api \
     --comment --body "<review body>"
   ```
   Use `--approve` instead of `--comment` only if there are zero Must-fix or Should-fix issues.
   Use `--request-changes` if there are any Must-fix issues.

7. Print the review body and the URL of the posted review comment.
