package com.budget.buddy.budget_buddy_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Budget Buddy API.
 * This class initializes and starts the Spring Boot application.
 */
@SpringBootApplication
public class Application {

  /**
   * Main entry point of the application.
   *
   * @param args command line arguments
   */
  static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
