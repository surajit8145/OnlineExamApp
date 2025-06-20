plugins {
    id("com.android.application") version "8.8.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
// project-level build.gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Correct Kotlin DSL syntax
    }
}