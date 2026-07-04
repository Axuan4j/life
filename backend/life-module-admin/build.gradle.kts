plugins {
    `java-library`
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

dependencies {
    api(project(":backend:life-common"))
    api(project(":backend:life-infra"))
    api(project(":backend:life-security"))
    api(project(":backend:life-module-user"))
    api(project(":backend:life-module-content"))
    api(project(":backend:life-module-feed"))
    api(project(":backend:life-module-message"))
    api(project(":backend:life-module-social"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform()
}
