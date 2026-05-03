package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends OwnableEntityRepository<CategoryEntity, UUID> {

  @Query("SELECT id FROM categories WHERE owner_id = :ownerId AND name ILIKE :pattern")
  List<UUID> findIdsByOwnerIdAndNameLike(@Param("ownerId") UUID ownerId, @Param("pattern") String pattern);
}
