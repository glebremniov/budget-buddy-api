package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.util.function.Supplier;

@FunctionalInterface
public interface RefreshTokenProvider extends Supplier<String> {

}
