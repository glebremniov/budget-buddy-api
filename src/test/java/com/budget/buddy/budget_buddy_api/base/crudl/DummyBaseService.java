package com.budget.buddy.budget_buddy_api.base.crudl;

public class DummyBaseService extends AbstractBaseService<DummyEntity, String, Object, Object, Object> {

  public DummyBaseService(
      BaseRepository<DummyEntity, String> repository,
      BaseMapper<DummyEntity, Object, Object, Object, Object> mapper
  ) {
    super(repository, mapper);
  }
}
