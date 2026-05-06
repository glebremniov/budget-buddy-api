package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryIntegrationTest extends BaseMvcIntegrationTest {

  private String userId;
  private String otherUserId;

  // ── helpers ────────────────────────────────────────────────────────────────

  Category createCategory(String ownerId, String name) throws Exception {
    return createCategory(ownerId, name, null);
  }

  Category createCategory(String ownerId, String name, Long monthlyBudget) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .with(jwtForUser(ownerId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name).monthlyBudget(monthlyBudget)))
        .exchange();

    return parseBody(result, Category.class);
  }

  @BeforeEach
  void setUp() {
    userId = createTestUser();
    otherUserId = createTestUser();
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Create {

    @Test
    void should_CreateCategory_When_ValidRequest() throws Exception {
      var category = createCategory(userId, "Groceries");

      assertThat(category.getId()).isNotNull();
      assertThat(category.getName()).isEqualTo("Groceries");
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.post().uri("/v1/categories")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryWrite().name("Groceries")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_ReturnLocationHeader_When_Created() {
      var result = mvc.post().uri("/v1/categories")
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryWrite().name("Groceries")))
          .exchange();

      assertThat(result)
          .hasStatus(HttpStatus.CREATED)
          .containsHeader("Location");
    }
  }

  @Nested
  class Read {

    @Test
    void should_ReturnCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.get().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var category = parseBody(result, Category.class);
      assertThat(category.getId()).isEqualTo(created.getId());
      assertThat(category.getName()).isEqualTo("Food");
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result = mvc.get().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_CategoryNotFound() {
      var result = mvc.get().uri("/v1/categories/{id}", UUID.randomUUID())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.get().uri("/v1/categories/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Replace {

    @Test
    void should_ReplaceCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");
      var newCategoryName = "Groceries";

      var result = mvc.put().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryWrite().name(newCategoryName)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var replaced = parseBody(result, Category.class);

      assertThat(replaced)
          .returns(created.getId(), Category::getId)
          .returns(newCategoryName, Category::getName);
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result = mvc.put().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryWrite().name("Hacked")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.put().uri("/v1/categories/{id}", created.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryWrite().name("Hacked")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Update {

    @Test
    void should_UpdateCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().name("Groceries")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.getName()).isEqualTo("Groceries");
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().name("Hacked")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().name("Hacked")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Delete {

    @Test
    void should_DeleteCategory_When_Owner() throws Exception {
      var created = createCategory(userId, "Food");

      var deleteResult = mvc.delete().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);

      var getResult = mvc.get().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(getResult).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserId, "Other's category");

      var result = mvc.delete().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userId, "Food");

      var result = mvc.delete().uri("/v1/categories/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class MonthlyBudget {

    @Test
    void should_PreserveBudget_When_PatchOmitsMonthlyBudget() throws Exception {
      var created = createCategory(userId, "Groceries", 50000L);

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().name("Renamed")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.getName()).isEqualTo("Renamed");
      assertThat(updated.getMonthlyBudget().get()).isEqualTo(50000L);
    }

    @Test
    void should_ClearBudget_When_PatchSendsNullMonthlyBudget() throws Exception {
      var created = createCategory(userId, "Groceries", 50000L);

      var update = new CategoryUpdate();
      update.setMonthlyBudget(JsonNullable.of(null));

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(update))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.getMonthlyBudget().get()).isNull();
    }

    @Test
    void should_UpdateBudget_When_PatchSendsNewMonthlyBudget() throws Exception {
      var created = createCategory(userId, "Groceries", 50000L);

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .with(jwtForUser(userId))
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().monthlyBudget(12345L)))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.getMonthlyBudget().get()).isEqualTo(12345L);
    }
  }

  @Nested
  class List {

    @Test
    void should_ReturnOnlyOwnCategoriesOrderedByName() throws Exception {
      createCategory(userId, "My Transport");
      createCategory(userId, "My Food");
      createCategory(otherUserId, "Other's Food");

      var result = mvc.get().uri("/v1/categories")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.getItems()).hasSize(2);
      assertThat(page.getItems())
          .extracting(Category::getName)
          .containsExactly("My Food", "My Transport");
    }

    @Test
    void should_ReturnEmptyList_When_NoCategories() throws Exception {
      var result = mvc.get().uri("/v1/categories")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.getItems()).isEmpty();
      assertThat(page.getMeta().getTotal()).isZero();
    }

    @Test
    void should_ReturnPagedResults_When_MultiplePages() throws Exception {
      createCategory(userId, "A");
      createCategory(userId, "B");
      createCategory(userId, "C");
      createCategory(userId, "D");

      var result = mvc.get().uri("/v1/categories?page=1&size=2")
          .with(jwtForUser(userId))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.getItems()).hasSize(2);
      assertThat(page.getMeta())
          .returns(1, PaginationMeta::getPage)
          .returns(2, PaginationMeta::getSize)
          .returns(4L, PaginationMeta::getTotal);
    }

    @Test
    void should_Return401_When_NotAuthenticated() {
      var result = mvc.get().uri("/v1/categories")
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }
}
