package com.budget.buddy.budget_buddy_api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginationMeta;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class CategoryIntegrationTest extends BaseMvcIntegrationTest {

  private String userToken;
  private String otherUserToken;

  // ── helpers ────────────────────────────────────────────────────────────────

  Category createCategory(String token, String name) throws Exception {
    var result = mvc.post().uri("/v1/categories")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new CategoryWrite().name(name)))
        .exchange();

    return parseBody(result, Category.class);
  }

  @BeforeEach
  void setUp() throws Exception {
    userToken = registerAndLogin("categoryuser");
    otherUserToken = registerAndLogin("otheruser");
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  @Nested
  class Create {

    @Test
    void should_CreateCategory_When_ValidRequest() throws Exception {
      var category = createCategory(userToken, "Groceries");

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
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
      var created = createCategory(userToken, "Food");

      var result = mvc.get().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var category = parseBody(result, Category.class);
      assertThat(category.getId()).isEqualTo(created.getId());
      assertThat(category.getName()).isEqualTo("Food");
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserToken, "Other's category");

      var result = mvc.get().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_CategoryNotFound() {
      var result = mvc.get().uri("/v1/categories/{id}", UUID.randomUUID())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userToken, "Food");

      var result = mvc.get().uri("/v1/categories/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class Replace {

    @Test
    void should_ReplaceCategory_When_Owner() throws Exception {
      var created = createCategory(userToken, "Food");
      var newCategoryName = "Groceries";

      var result = mvc.put().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
      var created = createCategory(otherUserToken, "Other's category");

      var result = mvc.put().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryWrite().name("Hacked")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userToken, "Food");

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
      var created = createCategory(userToken, "Food");

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().name("Groceries")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var updated = parseBody(result, Category.class);
      assertThat(updated.getName()).isEqualTo("Groceries");
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserToken, "Other's category");

      var result = mvc.patch().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json(new CategoryUpdate().name("Hacked")))
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userToken, "Food");

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
      var created = createCategory(userToken, "Food");

      var deleteResult = mvc.delete().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);

      var getResult = mvc.get().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(getResult).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return404_When_CategoryBelongsToOtherUser() throws Exception {
      var created = createCategory(otherUserToken, "Other's category");

      var result = mvc.delete().uri("/v1/categories/{id}", created.getId())
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void should_Return401_When_NotAuthenticated() throws Exception {
      var created = createCategory(userToken, "Food");

      var result = mvc.delete().uri("/v1/categories/{id}", created.getId())
          .exchange();

      assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class List {

    @Test
    void should_ReturnOnlyOwnCategoriesOrderedByName() throws Exception {
      createCategory(userToken, "My Transport");
      createCategory(userToken, "My Food");
      createCategory(otherUserToken, "Other's Food");

      var result = mvc.get().uri("/v1/categories")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
          .exchange();

      assertThat(result).hasStatus(HttpStatus.OK);
      var page = parseBody(result, PaginatedCategories.class);
      assertThat(page.getItems()).isEmpty();
      assertThat(page.getMeta().getTotal()).isZero();
    }

    @Test
    void should_ReturnPagedResults_When_MultiplePages() throws Exception {
      createCategory(userToken, "A");
      createCategory(userToken, "B");
      createCategory(userToken, "C");
      createCategory(userToken, "D");

      var result = mvc.get().uri("/v1/categories?page=1&size=2")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
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
