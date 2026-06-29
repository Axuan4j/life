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
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.7")
    api("io.minio:minio:8.5.13")
    api("org.lionsoul:ip2region:3.3.7")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
