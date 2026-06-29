import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

val androidApiBaseUrl = (localProperties.getProperty("life.api.baseUrl") ?: "http://10.0.2.2:8080").trimEnd('/') + "/"

android {
    namespace = "com.xuan.life.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xuan.life.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "API_BASE_URL", "\"$androidApiBaseUrl\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    compileOptions {
        // Android Java 编译和 Kotlin 编译目标必须保持一致，否则 Kotlin 2.x 会直接阻止构建。
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.runtime:runtime-saveable:1.7.8")
    // XML 主题资源 Theme.Material3.DayNight.NoActionBar 来自 Material Components，而不是 Compose 运行时包。
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.room:room-runtime:2.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
}
