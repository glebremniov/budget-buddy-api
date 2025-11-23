package com.budget.buddy.budget_buddy_api;

import com.budget.buddy.budget_buddy_api.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class Application {

  static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
