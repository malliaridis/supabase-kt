plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(Deps.Gradle.Plugins.androidBuildTools)
    implementation(Deps.JetBrains.Kotlin.gradlePlugin)
    implementation(Deps.JetBrains.Kotlin.serialization)
}

kotlin {
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}