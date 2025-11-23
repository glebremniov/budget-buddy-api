package com.budget.buddy.budget_buddy_api.service;

import com.budget.buddy.budget_buddy_api.mapper.CategoryMapper;
import com.budget.buddy.budget_buddy_api.model.Category;
import com.budget.buddy.budget_buddy_api.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for category operations.
 */
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
    this.categoryRepository = categoryRepository;
    this.categoryMapper = categoryMapper;
  }

  public List<Category> listCategories(int limit, int offset) {
    var entities = categoryRepository.findAll();
    var end = Math.min(offset + limit, entities.size());
    var page = entities.subList(offset, end);
    return categoryMapper.toCategories(page);
  }

  public long countCategories() {
    return categoryRepository.findAll().size();
  }

  public Category getCategory(String categoryId) {
    var entity = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    return categoryMapper.toCategory(entity);
  }

  @Transactional
  public Category createCategory(CategoryCreate request) {
    var saved = categoryRepository.save(categoryMapper.toEntity(request));
    return categoryMapper.toCategory(saved);
  }

  @Transactional
  public Category updateCategory(String categoryId, CategoryUpdate request) {
    var entity = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));

    if (request.getName() != null) {
      entity.setName(request.getName());
    }

    var saved = categoryRepository.save(entity);
    return categoryMapper.toCategory(saved);
  }

  @Transactional
  public void deleteCategory(String categoryId) {
    categoryRepository.deleteById(categoryId);
  }
}
