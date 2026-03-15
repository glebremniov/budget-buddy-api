package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryRepository extends BaseRepository<CategoryEntity, UUID> {

  /**
   * Find all categories by owner's ID.
   *
   * @param ownerId the ID of the owner
   * @return list of categories owned by the specified owner
   */
  Optional<CategoryEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

  /**
   * Find all categories by owner's ID.
   *
   * @param ownerId the ID of the owner
   * @return list of categories owned by the specified owner
   */
  Page<CategoryEntity> findAllByOwnerId(UUID ownerId, Pageable pageable);

  /**
   * Count the number of categories by owner's ID.
   *
   * @param ownerId the id of the owner
   * @return the count of categories owned by the specified owner
   */
  long countByOwnerId(UUID ownerId);
}
