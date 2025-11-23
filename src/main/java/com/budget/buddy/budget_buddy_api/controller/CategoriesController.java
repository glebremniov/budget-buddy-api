package com.budget.buddy.budget_buddy_api.controller;

import com.budget.buddy.budget_buddy_api.model.Category;
import com.budget.buddy.budget_buddy_api.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.service.CategoryService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Category controller for CRUDL operations on categories.
 */
@RestController
public class CategoriesController {

  private final CategoryService categoryService;

  public CategoriesController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * List all categories
   *
   * @param limit pagination limit
   * @param offset pagination offset
   * @return list of categories with pagination metadata
   */
  @GetMapping("/v1/categories")
  public ResponseEntity<?> listCategories(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {

    var categories = categoryService.listCategories(limit, offset);
    var total = categoryService.countCategories();

    var meta = new PaginationMeta();
    meta.setLimit(limit);
    meta.setOffset(offset);
    meta.setTotal((int) total);

    var response = new java.util.HashMap<>();
    response.put("items", categories);
    response.put("meta", meta);

    return ResponseEntity.ok(response);
  }

  /**
   * Create a new category
   *
   * @param request category creation request
   * @return created category with 201 status
   */
  @PostMapping("/v1/categories")
  public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryCreate request) {
    var created = categoryService.createCategory(request);
    return ResponseEntity
        .created(URI.create("/v1/categories/" + created.getId()))
        .body(created);
  }

  /**
   * Get a single category
   *
   * @param categoryId category ID
   * @return category details
   */
  @GetMapping("/v1/categories/{categoryId}")
  public ResponseEntity<Category> getCategory(@PathVariable String categoryId) {
    var category = categoryService.getCategory(categoryId);
    return ResponseEntity.ok(category);
  }

  /**
   * Update a category
   *
   * @param categoryId category ID
   * @param request category update request
   * @return updated category
   */
  @PutMapping("/v1/categories/{categoryId}")
  public ResponseEntity<Category> updateCategory(
      @PathVariable String categoryId,
      @Valid @RequestBody CategoryUpdate request) {

    var updated = categoryService.updateCategory(categoryId, request);
    return ResponseEntity.ok(updated);
  }

  /**
   * Delete a category
   *
   * @param categoryId category ID
   * @return 204 No Content
   */
  @DeleteMapping("/v1/categories/{categoryId}")
  public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
    categoryService.deleteCategory(categoryId);
    return ResponseEntity.noContent().build();
  }
}
