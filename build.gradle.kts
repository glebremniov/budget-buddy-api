plugins {
  java
  `jvm-test-suite`
  jacoco
  id("org.springframework.boot") version "4.0.4"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.openapi.generator") version "7.20.0"
  id("org.sonarqube") version "7.2.3.7755"
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
}

dependencies {
  val mapstructVersion = "1.6.3"
  val openApiVersion = "3.0.2"
  val lombokMapstructBindingVersion = "0.2.0"
  val jacksonDatabindNullableVersion = "0.2.9"

  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-liquibase")
  implementation("org.springdoc:springdoc-openapi-starter-common:${openApiVersion}")
  implementation("org.openapitools:jackson-databind-nullable:${jacksonDatabindNullableVersion}")
  implementation("org.mapstruct:mapstruct:${mapstructVersion}")

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

tasks.openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("${rootDir}/src/main/resources/openapi.yaml")
  outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
  apiPackage.set("com.budget.buddy.budget_buddy_api.generated.api")
  modelPackage.set("com.budget.buddy.budget_buddy_api.generated.model")
  configOptions.set(
    mapOf(
      "useSpringBoot4" to "true",
//      "useJSpecify" to "true", // https://github.com/OpenAPITools/openapi-generator/pull/23256
      "openApiNullable" to "true",
      "generateSupportingFiles" to "false",
      "useTags" to "true",
      "interfaceOnly" to "true",
      "skipOperationExample" to "true",
      "sourceFolder" to "src/main/java",
      "useJakartaEe" to "true"
    )
  )
}

sourceSets {
  main {
    java {
      srcDir(layout.buildDirectory.dir("generated/src/main/java"))
    }
  }
}

tasks.compileJava {
  dependsOn(tasks.openApiGenerate)
}

tasks.named("compileIntegrationTestJava") {
  dependsOn(tasks.openApiGenerate)
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

tasks.named("sonar") {
  dependsOn(tasks.jacocoTestReport)
}

sonar {
  properties {
    property("sonar.projectKey", "glebremniov_budget-buddy-api")
    property("sonar.organization", "glebremniov")
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath
    )
    property("sonar.tests", "src/test/java,src/integrationTest/java")
    property("sonar.coverage.exclusions", "**/generated/**")
    property("sonar.issue.ignore.multicriteria", "S119")
    property("sonar.issue.ignore.multicriteria.S119.ruleKey", "java:S119")
    property("sonar.issue.ignore.multicriteria.S119.resourceKey", "**/*.java")
  }
}