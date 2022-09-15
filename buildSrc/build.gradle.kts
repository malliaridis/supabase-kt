plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    with(Deps.Gradle.Plugins) {
        implementation(androidBuildTools)
    }
    implementation(Deps.JetBrains.Kotlin.gradlePlugin)
}

kotlin {
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}