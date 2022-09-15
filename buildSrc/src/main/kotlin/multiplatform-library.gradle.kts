plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "16"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Deps.JetBrains.KotlinX.serialization)

                with (Deps.Ktor.Client) {
                    implementation(core)
                    implementation(contentNegotiation)
                    implementation(auth)
                }
                implementation(Deps.Ktor.Serialization.json)
                implementation(Deps.JetBrains.KotlinX.dateTime)
                implementation(Deps.JetBrains.KotlinX.Coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Ktor.Client.mock)
                implementation(Deps.JetBrains.KotlinX.Coroutines.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(Deps.Ktor.Client.cio)
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(Deps.Ktor.Client.js)
            }
        }
        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                implementation(Deps.Ktor.Client.cio)
            }
        }
        val nativeTest by getting
    }
}
