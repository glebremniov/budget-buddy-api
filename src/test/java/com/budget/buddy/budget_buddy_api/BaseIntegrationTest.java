package com.budget.buddy.budget_buddy_api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class BaseIntegrationTest {

}
