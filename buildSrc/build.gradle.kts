plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    with(Deps.Gradle.Plugins) {
        implementation(kotlin)
        implementation(androidBuildTools)
        implementation(serialization)
    }
}

kotlin {
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}