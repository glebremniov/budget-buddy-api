package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityController;
import com.budget.buddy.budget_buddy_contracts.generated.api.CategoriesApi;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
public class CategoryController
    extends BaseEntityController<UUID, Category, CategoryWrite, CategoryUpdate, PaginatedCategories>
    implements CategoriesApi {

  public CategoryController(CategoryService service, CategoryMapper mapper) {
    super(service, mapper);
  }

  @Override
  public ResponseEntity<Void> deleteCategory(UUID categoryId) {
    return super.deleteInternal(categoryId);
  }

  @Override
  public ResponseEntity<Category> getCategory(UUID categoryId) {
    return super.readInternal(categoryId);
  }

  @Override
  public ResponseEntity<Category> replaceCategory(UUID categoryId, CategoryWrite categoryCreate) {
    return super.replaceInternal(categoryId, categoryCreate);
  }

  @Override
  public ResponseEntity<Category> updateCategory(UUID categoryId, CategoryUpdate categoryUpdate) {
    return super.updateInternal(categoryId, categoryUpdate);
  }

  @Override
  public ResponseEntity<PaginatedCategories> listCategories(Integer page, Integer size) {
    var pageable = PageRequest.of(page, size, Sort.Direction.ASC, "name");
    return super.listInternal(pageable);
  }

  @Override
  public ResponseEntity<Category> createCategory(CategoryWrite categoryCreate) {
    return super.createInternal(categoryCreate);
  }

  @Override
  protected URI createdURI(Category created) {
    return URI.create("/v1/categories/" + created.getId());
  }

}
