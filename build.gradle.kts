plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.2.1"
    pmd
    jacoco
}

group = "io.github.tomaszziola"
version = "0.0.1-SNAPSHOT"
description = "A simple CI/CD server in Spring Boot that listens for GitHub webhooks"

jacoco {
    toolVersion = "0.8.13"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.flywaydb:flyway-core:11.11.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.11.2")
    runtimeOnly("org.postgresql:postgresql")
    developmentOnly("com.h2database:h2")
    testImplementation("com.h2database:h2")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-inline:5.2.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)

}

tasks.named("check") {
    dependsOn("pmdMain", "pmdTest", "spotlessApply", "jacocoTestCoverageVerification")
}

springBoot {
    buildInfo()
}

spotless {
    java {
        googleJavaFormat()

        removeUnusedImports()

        trimTrailingWhitespace()
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.12.0"
    rulesMinimumPriority = 5
    ruleSetFiles = files("config/pmd/ruleset.xml")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${classpath.find { it.name.contains("byte-buddy-agent") }?.absolutePath}")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(false)
        html.required.set(true)
        csv.required.set(false)
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/dto/**",
                    "**/config/**",
                    "**/generated/**",
                    "**/*Application*",
                    "**/*Config*",
                    "**/repository/**",
                    "**/exception/**",
                    "**/entity/**"
                    )
            }
        })
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.99".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
    executionData.setFrom(tasks.jacocoTestReport.get().executionData)
}

