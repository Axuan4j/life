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
    api(project(":backend:life-module-message"))
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.ahocorasick:ahocorasick:0.6.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
