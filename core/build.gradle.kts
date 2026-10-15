import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure-Kotlin game engine: sequence generation, scoring and level progression.
// No Android dependencies, so it can be unit tested on any JVM.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.junit)
}
