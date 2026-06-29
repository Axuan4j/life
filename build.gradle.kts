import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    base
    id("org.springframework.boot") version "3.5.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

val springBootVersion = "3.5.0"

group = "com.xuan.life"
version = "1.0.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    plugins.withId("io.spring.dependency-management") {
        extensions.configure<DependencyManagementExtension> {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            }
        }
    }

    plugins.withId("java") {
        tasks.withType<JavaCompile>().configureEach {
            // Spring MVC 在运行时可能需要读取方法参数名做绑定，这里统一开启 -parameters。
            options.compilerArgs.add("-parameters")
            // Windows 默认编译编码常常不是 UTF-8，源码中的中文提示如果按本机编码编译会直接变成乱码。
            options.encoding = "UTF-8"
        }
        dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    plugins.withId("java-library") {
        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-parameters")
            options.encoding = "UTF-8"
        }
        dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
}

//tasks.register<Delete>("clean") {
//    delete(layout.buildDirectory)
//}
