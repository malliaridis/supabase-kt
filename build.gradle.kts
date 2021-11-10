val ktorVersion by rootProject.extra { "1.6.5" }

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

group = "io.supabase"
version = "0.0.1"

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}