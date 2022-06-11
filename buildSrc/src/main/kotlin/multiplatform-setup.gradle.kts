plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

kotlin {
    jvm("desktop")
    android()
    ios()

    js(IR) {
        browser()
    }

    sourceSets {

        named("commonMain") {
            dependencies {
                with (Deps.JetBrains.Kotlinx) {
                    implementation(dateTime)
                    implementation(coroutinesCore)
                }
            }
        }

        named("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        named("androidTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testJUnit)
            }
        }

        named("desktopTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testJUnit)
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = Versions.jvmTarget
    }
}
