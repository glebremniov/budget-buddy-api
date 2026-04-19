plugins {
  java
  `jvm-test-suite`
  jacoco
  id("org.springframework.boot") version "4.0.5"
  id("io.spring.dependency-management") version "1.1.7"
}

group = "com.budget.buddy"
description = "API for Budget Buddy App"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/budget-buddy-org/budget-buddy-contracts")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
  }
}

dependencies {
  val mapstructVersion = "1.6.3"
  val lombokMapstructBindingVersion = "0.2.0"
  val jacksonDatabindNullableVersion = "0.2.10"
  val budgetBuddyContractsVersion = "3.0.0"
  implementation("com.budgetbuddy:budget-buddy-contracts:${budgetBuddyContractsVersion}")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-liquibase")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.openapitools:jackson-databind-nullable:${jacksonDatabindNullableVersion}")
  implementation("org.mapstruct:mapstruct:${mapstructVersion}")
  implementation("com.github.ben-manes.caffeine:caffeine")


  compileOnly("org.projectlombok:lombok")

  runtimeOnly("org.postgresql:postgresql")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("org.springframework.boot:spring-boot-docker-compose")

  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}")
  annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testCompileOnly("org.projectlombok:lombok")
  testCompileOnly("org.mapstruct:mapstruct:${mapstructVersion}")
  testAnnotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
  testAnnotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}")
}

@Suppress("UnstableApiUsage")
testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }

    register<JvmTestSuite>("integrationTest") {
      useJUnitJupiter()
      dependencies {
        implementation(project())
        implementation("org.springframework.boot:spring-boot-starter-liquibase-test")
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
        implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
        implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
        implementation("org.springframework.boot:spring-boot-starter-security-test")
        implementation("org.springframework.boot:spring-boot-testcontainers")
        implementation("org.testcontainers:testcontainers-junit-jupiter")
        implementation("org.testcontainers:testcontainers-postgresql")
      }

      targets {
        all {
          testTask.configure {
            shouldRunAfter(test)
          }
        }
      }
    }
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
  named("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
  }
  named("integrationTestCompileOnly") {
    extendsFrom(configurations.testCompileOnly.get())
  }
  named("integrationTestAnnotationProcessor") {
    extendsFrom(configurations.testAnnotationProcessor.get())
  }
}

tasks.named<Test>("test") {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.named<Test>("integrationTest") {
  finalizedBy(tasks.jacocoTestReport)
}

@Suppress("UnstableApiUsage")
tasks.named("check") {
  dependsOn(testing.suites.named("test"))
  dependsOn(testing.suites.named("integrationTest"))
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required = true
  }
  executionData(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
  classDirectories.setFrom(files(classDirectories.files.map {
    fileTree(it).matching {
      exclude("**/generated/**")
    }
  }))
}
