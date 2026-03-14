package com.budget.buddy.budget_buddy_api.security.refresh.token;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseEntityListener;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenEntityListener extends BaseEntityListener<RefreshTokenEntity, String> {

  public RefreshTokenEntityListener(RefreshTokenProvider tokenProvider) {
    super(tokenProvider);
  }
}
