package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.generated.api.CategoriesApi;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.generated.model.V1CategoriesGet200Response;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Category controller for CRUDL operations on categories.
 */
@RestController
public class CategoryController implements CategoriesApi {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @Override
  public ResponseEntity<Void> v1CategoriesCategoryIdDelete(String categoryId) {
    categoryService.delete(UUID.fromString(categoryId));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Category> v1CategoriesCategoryIdGet(String categoryId) {
    var category = categoryService.read(UUID.fromString(categoryId));
    return ResponseEntity.ok(category);
  }

  @Override
  public ResponseEntity<Category> v1CategoriesCategoryIdPut(String categoryId, CategoryUpdate categoryUpdate) {
    var updated = categoryService.update(UUID.fromString(categoryId), categoryUpdate);
    return ResponseEntity.ok(updated);
  }

  @Override
  public ResponseEntity<V1CategoriesGet200Response> v1CategoriesGet(Integer limit, Integer offset) {
    var categories = categoryService.list(limit, offset);
    var total = categoryService.count();

    var meta = new PaginationMeta();
    meta.setLimit(limit);
    meta.setOffset(offset);
    meta.setTotal((int) total);

    var response = new V1CategoriesGet200Response()
        .items(categories)
        .meta(meta);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Category> v1CategoriesPost(CategoryCreate categoryCreate) {
    var created = categoryService.create(categoryCreate);
    return ResponseEntity
        .created(URI.create("/v1/categories/" + created.getId()))
        .body(created);
  }
}
