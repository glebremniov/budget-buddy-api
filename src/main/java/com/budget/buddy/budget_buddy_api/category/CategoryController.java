package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseController;
import com.budget.buddy.budget_buddy_api.generated.api.CategoriesApi;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.generated.model.PaginatedCategories;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController
    extends BaseController<UUID, Category, CategoryCreate, CategoryUpdate, PaginatedCategories>
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
  public ResponseEntity<Category> updateCategory(UUID categoryId, CategoryUpdate categoryUpdate) {
    return super.updateInternal(categoryId, categoryUpdate);
  }

  @Override
  public ResponseEntity<PaginatedCategories> listCategories(Integer limit, Integer offset) {
    return super.listInternal(limit, offset);
  }

  @Override
  public ResponseEntity<Category> createCategory(CategoryCreate categoryCreate) {
    return super.createInternal(categoryCreate);
  }

  @Override
  protected URI createdURI(Category created) {
    return URI.create("/v1/categories/" + created.getId());
  }

}
