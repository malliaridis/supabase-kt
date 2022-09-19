plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("maven-publish")
}

kotlin {
    jvm() {
        compilations.all {
            kotlinOptions.jvmTarget = Deps.jvmTarget
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    android {
        // Publish only release variant
        publishLibraryVariants("release")
    }

    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }

    publishing {
        publications {
            // Apply group ID and version to all publications
            group = Deps.group
            version = Deps.version
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(Deps.JetBrains.KotlinX.serialization)

                with (Deps.Ktor.Client) {
                    api(core)
                    api(contentNegotiation)
                    api(auth)
                }
                api(Deps.Ktor.Serialization.json)
                api(Deps.JetBrains.KotlinX.dateTime)
                api(Deps.JetBrains.KotlinX.Coroutines.core)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Ktor.Client.mock)
                implementation(Deps.JetBrains.KotlinX.Coroutines.test)
            }
        }

        named("jvmMain") {
            dependencies {
                api(Deps.Ktor.Client.cio)
            }
        }
        named("jvmTest")

        named("jsMain") {
            dependencies {
                api(Deps.Ktor.Client.js)
            }
        }
        named("jsTest")
    }

    // TODO See if below task changes anything when building / publishing project
//    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = Versions.jvmTarget
//    }
}

android {
    compileSdk = Deps.Android.androidCompileSdk

    defaultConfig {
        minSdk = Deps.Android.androidMinSdk
        targetSdk = Deps.Android.androidTargetSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
    }
}
