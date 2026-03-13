package com.budget.buddy.budget_buddy_api.base.crudl;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * BaseRepository is a generic interface that defines common CRUD operations for entities that extend BaseEntity.
 *
 * @param <E> the type of the entity
 * @param <ID> the type of the entity's identifier
 */
@NoRepositoryBean
@SuppressWarnings("java:S119")
public interface BaseRepository<E extends BaseEntity<ID>, ID> extends ListCrudRepository<E, ID> {

}
