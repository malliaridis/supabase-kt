plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}
kotlin {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = Deps.jvmTarget
    }
}

dependencies {
    implementation(project(":supabase"))
}
