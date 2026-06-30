pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "life"

include(
    "backend:life-boot",
    "backend:life-common",
    "backend:life-security",
    "backend:life-infra",
    "backend:life-module-admin",
    "backend:life-module-user",
    "backend:life-module-content",
    "backend:life-module-message",
    "backend:life-module-social",
    "backend:life-module-feed",
)
