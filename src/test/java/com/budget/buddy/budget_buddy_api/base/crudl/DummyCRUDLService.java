package com.budget.buddy.budget_buddy_api.base.crudl;

public class DummyCRUDLService extends AbstractCRUDLService<DummyEntity, String, Object, Object, Object> {

  public DummyCRUDLService(
      BaseRepository<DummyEntity, String> repository,
      BaseMapper<DummyEntity, Object, Object, Object, Object> mapper
  ) {
    super(repository, mapper);
  }
}
