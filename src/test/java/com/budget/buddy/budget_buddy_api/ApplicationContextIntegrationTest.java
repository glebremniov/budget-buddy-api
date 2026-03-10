package com.budget.buddy.budget_buddy_api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

class ApplicationContextIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private ApplicationContext context;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
  }
}
