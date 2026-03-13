package com.budget.buddy.budget_buddy_api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
@RequiredArgsConstructor
public class AuthorityRepository {

  private static final String DEFAULT_AUTHORITY = "USER";
  private static final String INSERT_QUERY = "INSERT INTO authorities (username, authority) VALUES (?, ?)";

  private final JdbcTemplate jdbcTemplate;

  public void addDefaultAuthorityToUser(String username) {
    addAuthorityToUser(username, DEFAULT_AUTHORITY);
  }

  void addAuthorityToUser(String username, String authority) {
    jdbcTemplate.update(INSERT_QUERY, username, authority);
  }

}
