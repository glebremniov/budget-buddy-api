package com.budget.buddy.budget_buddy_api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfig.class)
public abstract class BaseIntegrationTest {

}
