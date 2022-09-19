object Deps {

    /**
     * The libraries' group name
     */
    const val group = "io.supabase"

    /**
     * The libraries' version
     * TODO Get version from environment / pipeline
     */
    const val version: String = "0.1.0"

    /**
     * JVM target of compilation
     */
    const val jvmTarget = "11"

    object Gradle {
        object Plugins {
            const val androidBuildTools = "com.android.tools.build:gradle:7.2.2"
        }
    }

    object Android {
        const val androidMinSdk = 21
        const val androidCompileSdk = 33
        const val androidTargetSdk = androidCompileSdk
    }

    object AndroidX {

        object AppCompat {
            const val appCompat = "androidx.appcompat:appcompat:1.4.2"
        }

        object Core {
            const val coreKtx = "androidx.core:core-ktx:1.8.0"
        }
    }

    object JetBrains {
        object Kotlin {
            const val VERSION = "1.7.10"
            
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$VERSION"
            const val serialization = "org.jetbrains.kotlin:kotlin-serialization:$VERSION"
            const val testCommon = "org.jetbrains.kotlin:kotlin-test-common:$VERSION"
            const val testJUnit = "org.jetbrains.kotlin:kotlin-test-junit:$VERSION"
            const val testJs = "org.jetbrains.kotlin:kotlin-test-js:$VERSION"
            const val testAnnotationsCommon = "org.jetbrains.kotlin:kotlin-test-annotations-common:$VERSION"
        }

        object KotlinX {
            object Coroutines {
                private const val VERSION = "1.6.4"

                const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$VERSION"
                const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$VERSION"
            }

            private const val dateTimeVersion = "0.4.0"
            const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion"
            const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0"
        }
    }

    object Ktor {
        private const val VERSION = "2.1.1" // "2.1.1-eap-496"

        object Client {

            const val core = "io.ktor:ktor-client-core:$VERSION"
            const val auth = "io.ktor:ktor-client-auth:$VERSION"
            // TODO See if this dependency is used at all
            const val json = "io.ktor:ktor-client-json:$VERSION"
            const val logging = "io.ktor:ktor-client-logging:$VERSION"
            const val contentNegotiation = "io.ktor:ktor-client-content-negotiation:$VERSION"
            const val websockets = "io.ktor:ktor-client-websockets:$VERSION"

            const val mock = "io.ktor:ktor-client-mock:$VERSION"

            // Engines
            const val android = "io.ktor:ktor-client-android:$VERSION"
            const val java = "io.ktor:ktor-client-java:$VERSION"
            const val ios = "io.ktor:ktor-client-ios:$VERSION"
            const val js = "io.ktor:ktor-client-js:$VERSION"
            const val cio = "io.ktor:ktor-client-cio:$VERSION"
            const val curl = "io.ktor:ktor-client-curl:$VERSION"
            const val darwin = "io.ktor:ktor-client-darwin:$VERSION"
        }

        object Serialization {
            const val json = "io.ktor:ktor-serialization-kotlinx-json:$VERSION"
        }
    }

    object Test {
        const val junit = "junit:junit:$4.13.2"
        const val androidXTestJUnit = "androidx.test.ext:junit:1.1.3"

        const val mockito = "org.mockito:mockito-inline:4.6.1"
        const val testCore = "androidx.test:core:$1.4.0"

        // TODO Remove if not needed anymore
        const val robolectric = "org.robolectric:robolectric:4.8.1"
    }
}