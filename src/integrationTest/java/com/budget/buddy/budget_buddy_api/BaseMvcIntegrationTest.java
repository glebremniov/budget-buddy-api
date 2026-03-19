package com.budget.buddy.budget_buddy_api;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.LoginRequest;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

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

  protected String registerAndLogin(String username, String password) throws Exception {
    register(username, password);
    return login(username, password).getAccessToken();
  }

  protected void register(String username, String password) {
    var exchange = mvc.post().uri("/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new RegisterRequest().username(username).password(password)))
        .exchange();

    assertThat(exchange).hasStatus(HttpStatus.CREATED);
  }

  protected AuthToken login(String username, String password) throws Exception {
    var exchange = mvc.post().uri("/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new LoginRequest().username(username).password(password)))
        .exchange();

    assertThat(exchange).hasStatus(HttpStatus.OK);

    return objectMapper.readValue(
        exchange.getResponse().getContentAsString(), AuthToken.class);
  }

}
