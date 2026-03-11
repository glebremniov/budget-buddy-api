package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.List;

/**
 * Generic CRUDL (Create, Read, Update, Delete, List) service interface for managing entities.
 *
 * @param <ID> The type of the unique identifier for the entities managed by the service.
 * @param <R> The type of the resource/model returned by the service methods.
 * @param <C> The type of the create request object used for creating new entities.
 * @param <U> The type of the update request object used for updating existing entities.
 */
public interface BaseService<ID, R, C, U> {

  /**
   * Create a new entity based on the provided createRequest request object.
   *
   * @param createRequest The createRequest request object containing the data for the new entity.
   * @return The created resource/model representing the new entity.
   */
  R create(C createRequest);

  /**
   * Read an existing entity by its unique identifier.
   *
   * @param id The unique identifier of the entity to read.
   * @return The resource/model representing the entity with the specified ID.
   * @throws EntityNotFoundException If no entity with the specified ID exists.
   */
  R read(ID id) throws EntityNotFoundException;

  /**
   * Partially update an existing entity identified by its unique identifier.
   *
   * @param id The unique identifier of the entity to patch.
   * @param patchRequest The patch request object containing only the fields to update.
   * @return The updated resource/model representing the modified entity.
   * @throws EntityNotFoundException If no entity with the specified ID exists.
   */
  R update(ID id, U patchRequest) throws EntityNotFoundException;

  /**
   * Delete an existing entity by its unique identifier.
   *
   * @param id The unique identifier of the entity to delete.
   * @throws EntityNotFoundException If no entity with the specified ID exists.
   */
  void delete(ID id) throws EntityNotFoundException;

  /**
   * List all existing entities.
   *
   * @return A list of resources/models representing all existing entities.
   */
  List<R> list();

  /**
   * List entities with pagination support.
   *
   * @param page The page number to retrieve (0-based index).
   * @param size The number of entities to retrieve per page.
   * @return A list of resources/models representing the entities for the specified page and size.
   */
  List<R> list(int page, int size);

  /**
   * Count the total number of existing entities.
   *
   * @return The total count of existing entities.
   */
  long count();

}
