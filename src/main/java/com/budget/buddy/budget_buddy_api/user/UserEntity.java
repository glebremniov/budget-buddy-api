package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseEntity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * User entity representing a user account in the system. Uses Spring Data JDBC for data access.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity extends BaseEntity<UUID> {

  @Column("username")
  private String username;

  @Column("password")
  private String password;

  @Column("enabled")
  private boolean enabled;

}
