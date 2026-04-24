package com.budget.buddy.budget_buddy_api;

import com.budget.buddy.budget_buddy_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class BaseOwnableIntegrationTest extends BaseIntegrationTest {

  private static final Supplier<String> RANDOM_OIDC_SUBJECT_SUPPLIER = () -> "sub_" + UUID.randomUUID();
  private static final Supplier<String> RANDOM_OIDC_ISSUER_SUPPLIER = () -> "iss_" + UUID.randomUUID();

  @Autowired
  protected UserRepository userRepository;

  protected UUID upsertRandomUser() {
    return userRepository.upsert(
        UUID.randomUUID(),
        RANDOM_OIDC_SUBJECT_SUPPLIER.get(),
        RANDOM_OIDC_ISSUER_SUPPLIER.get()
    );
  }

}
