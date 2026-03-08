package com.budget.buddy.budget_buddy_api.base.exception;

import java.util.NoSuchElementException;

public class EntityNotFoundException extends NoSuchElementException {

  public EntityNotFoundException(String message) {
    super(message);
  }

}
