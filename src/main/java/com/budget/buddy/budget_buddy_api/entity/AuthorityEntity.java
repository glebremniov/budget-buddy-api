package com.budget.buddy.budget_buddy_api.entity;

import org.springframework.data.relational.core.mapping.Table;

/**
 * Authority entity representing user roles/permissions. Used by Spring Security for role-based access control.
 */
@Table("authorities")
public class AuthorityEntity {

  private String username;
  private String authority;

  public AuthorityEntity() {
  }

  public AuthorityEntity(String username, String authority) {
    this.username = username;
    this.authority = authority;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAuthority() {
    return authority;
  }

  public void setAuthority(String authority) {
    this.authority = authority;
  }
}
