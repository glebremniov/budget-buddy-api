Scaffold a new CRUDL domain feature end-to-end. Accepts the domain name as the argument (e.g. `/new-feature budget`).

**Usage:** `/new-feature <domain-name>`

**Steps:**

1. Derive names from the argument:
   - Domain: `<domain-name>` (e.g. `budget`)
   - Package: `com.budget.buddy.budget_buddy_api.<domain-name>`
   - Entity class: `<Domain>Entity`
   - DTO classes: as defined in the external `budget-buddy-contracts` library (already on the classpath)
   - Controller interface: the generated API interface from the contracts library; use proper English pluralization (e.g. `CategoriesApi`, `TransactionsApi`, `BudgetsApi`) — do **not** blindly append `s`

2. **Contracts first** — this project consumes models and API interfaces from the external `budget-buddy-contracts` library. There is no local `openapi.yaml` to edit and no `openApiGenerate` task. Before scaffolding, confirm that the required API interface and DTOs for this domain already exist on the classpath (check `build/generated` or the contracts source). If they are missing, stop and tell the user to update the contracts library first.

3. **Liquibase migration** — determine the next migration number by listing files in `src/main/resources/db/changelog/migrations/` and incrementing the highest number. Zero-pad to 3 digits. Create the file `src/main/resources/db/changelog/migrations/<next-number>-create-<domain-name>-table.sql` (use hyphens, not underscores). Include at minimum: `id` (UUID primary key), `owner_id` (UUID not null), `created_at`, `updated_at`, plus domain-specific columns. Register the new file in `src/main/resources/db/changelog/db.changelog-master.yaml` following the same `include` format as existing entries.

4. **Entity** — create `src/main/java/com/budget/buddy/budget_buddy_api/<domain-name>/<Domain>Entity.java` implementing `OwnableEntity`. Use Lombok `@Data`, `@Table`. Map every column with `@Column` where needed.

5. **Repository** — create `<Domain>Repository` extending `BaseEntityRepository<<Domain>Entity>`.

6. **Mapper** — create `<Domain>Mapper` extending `BaseEntityMapper<<Domain>Entity, <Read>, <Create>, <Update>, <List>>`. Annotate with `@Mapper(componentModel = "spring")`.

7. **Service** — create `<Domain>Service` extending `OwnableEntityService<<Domain>Entity, <Read>, <Create>, <Update>, <Domain>Repository>`. Wire in validators as a `Set<BaseEntityValidator<<Domain>Entity>>`.

8. **Controller** — create `<Domain>Controller` implementing the contracts API interface (e.g. `BudgetsApi`) and extending `BaseEntityController<ID, Read, Create, Update, List>` with all 5 type parameters filled in from the contracts DTOs. Override `createdURI()` to return the correct resource path. Delegate all methods to the `*Internal()` helpers.

9. **Integration tests** — create `src/integrationTest/java/com/budget/buddy/budget_buddy_api/<domain-name>/<Domain>IntegrationTest.java` extending `BaseMvcIntegrationTest`. Cover: create (201), read (200), update (200), delete (204), list (200), and auth guard (401 when no token).

10. **Compile check** — run `./gradlew compileJava compileTestJava compileIntegrationTestJava` and fix any errors before finishing.

11. Report what was created and remind the user to run `./gradlew integrationTest` to validate end-to-end.

**Conventions to follow:**
- `var` for all local variable declarations
- No error handling for scenarios the framework already covers
- No extra abstractions beyond what is described above
- Throw `EntityNotFoundException` from service for missing entities — do not catch in controller
