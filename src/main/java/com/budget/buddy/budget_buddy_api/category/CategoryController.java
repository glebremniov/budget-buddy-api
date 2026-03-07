package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractCRUDLController;
import com.budget.buddy.budget_buddy_api.generated.api.CategoriesApi;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import com.budget.buddy.budget_buddy_api.generated.model.V1CategoriesGet200Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController
    extends AbstractCRUDLController<CategoryEntity, UUID, Category, CategoryCreate, CategoryUpdate, V1CategoriesGet200Response>
    implements CategoriesApi {

  public CategoryController(CategoryService categoryService) {
    super(categoryService);
  }

  @Override
  public ResponseEntity<Void> v1CategoriesCategoryIdDelete(String categoryId) {
    return super.deleteInternal(categoryId);
  }

  @Override
  public ResponseEntity<Category> v1CategoriesCategoryIdGet(String categoryId) {
    return super.readInternal(categoryId);
  }

  @Override
  public ResponseEntity<Category> v1CategoriesCategoryIdPut(String categoryId, CategoryUpdate categoryUpdate) {
    return super.updateInternal(categoryId, categoryUpdate);
  }

  @Override
  public ResponseEntity<V1CategoriesGet200Response> v1CategoriesGet(Integer limit, Integer offset) {
    return super.listInternal(limit, offset);
  }

  @Override
  public ResponseEntity<Category> v1CategoriesPost(CategoryCreate categoryCreate) {
    return super.createInternal(categoryCreate);
  }

  @Override
  protected URI createdURI(Category created) {
    return URI.create("/v1/categories/" + created.getId());
  }

  @Override
  protected V1CategoriesGet200Response listResponse(List<Category> items, PaginationMeta meta) {
    return new V1CategoriesGet200Response()
        .items(items)
        .meta(meta);
  }

  @Override
  protected UUID fromString(String id) {
    return UUID.fromString(id);
  }
}
