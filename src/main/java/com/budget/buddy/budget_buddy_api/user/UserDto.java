package com.budget.buddy.budget_buddy_api.user;

import java.util.UUID;

public record UserDto(UUID id, String username, boolean enabled) {

}
