package com.budget.buddy.budget_buddy_api.base.crudl;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * BaseRepository is a generic interface that defines common CRUD operations for entities that extend BaseEntity.
 *
 * @param <E> the type of the entity
 * @param <ID> the type of the entity's identifier
 */
@NoRepositoryBean
@SuppressWarnings("java:S119")
public interface BaseRepository<E extends BaseEntity<ID>, ID> extends Repository<E, ID> {

  /**
   * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the entity instance completely.
   *
   * @param entity the entity to save
   * @return the saved entity
   */
  <S extends E> S save(S entity);

  /**
   * Saves all given entities.
   *
   * @param entities the entities to save
   * @return the saved entities
   */
  <S extends E> Iterable<S> saveAll(Iterable<S> entities);

  /**
   * Retrieves an entity by its id.
   *
   * @param id the id of the entity to retrieve
   * @return an Optional containing the found entity or empty if not found
   */
  Optional<E> findById(ID id);

  /**
   * Returns whether an entity with the given id exists.
   *
   * @param id the id of the entity to check for existence
   * @return true if an entity with the given id exists, false otherwise
   */
  boolean existsById(ID id);

  /**
   * Returns all instances of the type.
   *
   * @return all entities
   */
  List<E> findAll();

  /**
   * Returns all instances of the type with the given IDs.
   *
   * @param ids the IDs of the entities to retrieve
   * @return the found entities
   */
  List<E> findAllById(Iterable<ID> ids);

  /**
   * Returns the number of entities available.
   *
   * @return the number of entities
   */
  long count();

  /**
   * Deletes the entity with the given id.
   *
   * @param id the id of the entity to delete
   */
  void deleteById(ID id);

  /**
   * Deletes a given entity.
   *
   * @param entity the entity to delete
   */
  void delete(E entity);

  /**
   * Deletes entities with the given ids.
   *
   * @param ids the ids of the entities to delete
   */
  void deleteAllById(Iterable<? extends ID> ids);

  /**
   * Deletes the given entities.
   *
   * @param entities the entities to delete
   */
  void deleteAll(Iterable<? extends E> entities);

  /**
   * Deletes all entities managed by the repository.
   */
  void deleteAll();

}
