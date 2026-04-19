package com.budget.buddy.budget_buddy_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@AutoConfigureMockMvc
public abstract class BaseMvcIntegrationTest extends BaseIntegrationTest {

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvcTester mvc;

  protected String json(Object obj) {
    return objectMapper.writeValueAsString(obj);
  }

  protected <T> T parseBody(MvcTestResult result, Class<T> type) throws Exception {
    return objectMapper.readValue(result.getResponse().getContentAsString(), type);
  }

  /**
   * Creates a unique OIDC subject for test isolation.
   * The provisioning filter will auto-create the local user on first request.
   */
  protected String createTestUser() {
    return "test-sub-" + UUID.randomUUID();
  }

  /**
   * Returns a JWT request post-processor for the given OIDC subject.
   */
  protected static RequestPostProcessor jwtForUser(String oidcSubject) {
    return jwt().jwt(j -> j.subject(oidcSubject));
  }

}
