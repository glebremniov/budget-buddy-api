plugins {
  java
  jacoco
  id("org.springframework.boot") version "4.0.3"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.openapi.generator") version "7.3.0"
  id("org.sonarqube") version "7.2.3.7755"
}

group = "com.budget.buddy"
version = "0.0.1-SNAPSHOT"
description = "API for Budget Buddy App"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
}

dependencies {
  val mapstructVersion = "1.6.3"
  val openApiVerification = "3.0.1"
  val lombokMapstructBindingVersion = "0.2.0"
  val jacksonDatabindNullableVersion = "0.2.9"

  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-liquibase")
  implementation("org.springdoc:springdoc-openapi-starter-common:${openApiVerification}")
  implementation("org.openapitools:jackson-databind-nullable:${jacksonDatabindNullableVersion}")

  compileOnly("org.projectlombok:lombok")
  compileOnly("org.mapstruct:mapstruct:${mapstructVersion}")

  runtimeOnly("org.postgresql:postgresql")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("org.springframework.boot:spring-boot-docker-compose")

  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}")
  annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
  testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy("jacocoTestReport")
}

tasks.openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("${rootDir}/src/main/resources/openapi.yaml")
  outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
  apiPackage.set("com.budget.buddy.budget_buddy_api.generated.api")
  modelPackage.set("com.budget.buddy.budget_buddy_api.generated.model")
  configOptions.set(
    mapOf(
      "useSpringBoot4" to "true",
      "generateSupportingFiles" to "false",
      "useTags" to "true",
      "interfaceOnly" to "true",
      "skipOperationExample" to "true",
      "sourceFolder" to "src/main/java",
      "useJakartaEe" to "true"
    )
  )
}

// Add generated sources to sourceSet
sourceSets {
  main {
    java {
      srcDir(layout.buildDirectory.dir("generated/src/main/java"))
    }
  }
}

// Ensure openapi task runs before compilation
tasks.compileJava {
  dependsOn(tasks.openApiGenerate)
}

tasks.withType<JacocoReport> {
  reports {
    xml.required = true
  }
}
