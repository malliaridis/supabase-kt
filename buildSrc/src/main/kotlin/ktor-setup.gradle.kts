plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
                with (Deps.Ktor.Client) {
                    implementation(core)
                    implementation(contentNegotiation)
                    implementation(auth)
                }
                implementation(Deps.Ktor.Serialization.json)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(Deps.Test.ktorMock)
            }
        }

        named("androidMain") {
            dependencies {
                implementation(Deps.Ktor.Client.cio)
            }
        }

        named("desktopMain") {
            dependencies {
                implementation(Deps.Ktor.Client.cio)
            }
        }

        named("jsMain") {
            dependencies {
                implementation(Deps.Ktor.Client.js)
            }
        }

        named("iosMain") {
            dependencies {
                implementation(Deps.Ktor.Client.darwin)
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
