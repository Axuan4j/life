plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":backend:life-common"))
    implementation(project(":backend:life-security"))
    implementation(project(":backend:life-infra"))
    implementation(project(":backend:life-module-admin"))
    implementation(project(":backend:life-module-user"))
    implementation(project(":backend:life-module-content"))
    implementation(project(":backend:life-module-message"))
    implementation(project(":backend:life-module-social"))
    implementation(project(":backend:life-module-feed"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform()
}
