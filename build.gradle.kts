val jvmTarget by rootProject.extra { 11 }
val globalGroup by rootProject.extra { "io.supabase" }
val globalVersion by rootProject.extra { "0.0.1" }

val ktorVersion by rootProject.extra { "1.6.5" }
val datetimeVersion by rootProject.extra { "0.3.1" }
val coroutineVersion by rootProject.extra { "1.5.2" }

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.android.tools.build:gradle:7.0.3")
    }
}

group = rootProject.extra["globalGroup"].toString()
version = rootProject.extra["globalVersion"].toString()

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}