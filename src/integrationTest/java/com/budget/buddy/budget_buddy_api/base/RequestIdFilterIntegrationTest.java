package com.budget.buddy.budget_buddy_api.base;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterIntegrationTest extends BaseMvcIntegrationTest {

  @Test
  void should_ReturnRequestId_When_NotProvided() {
    var result = mvc.get().uri("/v1/categories")
        .with(jwtForUser(createTestUser()))
        .exchange();

    assertThat(result.getResponse().getHeader(RequestIdFilter.HEADER)).isNotBlank();
  }

  @Test
  void should_EchoRequestId_When_ProvidedByClient() {
    var clientId = "my-trace-id-123";

    var result = mvc.get().uri("/v1/categories")
        .with(jwtForUser(createTestUser()))
        .header(RequestIdFilter.HEADER, clientId)
        .exchange();

    assertThat(result.getResponse().getHeader(RequestIdFilter.HEADER)).isEqualTo(clientId);
  }

  @Test
  void should_ReturnRequestId_On_ErrorResponse() {
    var result = mvc.get().uri("/v1/categories?page=0&size=99999")
        .with(jwtForUser(createTestUser()))
        .exchange();

    assertThat(result.getResponse().getHeader(RequestIdFilter.HEADER)).isNotBlank();
  }
}
