object Versions {
    const val androidMinSdk = 21
    const val androidCompileSdk = 31
    const val androidTargetSdk = androidCompileSdk

    const val jvmTarget = "11"

    const val kotlinCoroutines = "1.6.1"
    const val kotlinDateTime = "0.3.2" // 0.3.3

    const val junit = "4.13.2"
    const val androidXTestJUnit = "1.1.3"
    const val testCore = "1.4.0"
    const val mockito = "4.6.1"
    const val robolectric = "4.8.1"

    const val lifecycleKtx = "2.4.0-rc01"
    const val lifecycleRuntimeKtx = lifecycleKtx
    const val lifecycleViewmodelKtx = lifecycleKtx
}

object Deps {

    object Gradle {
        object Plugins {
            const val androidBuildTools = "com.android.tools.build:gradle:7.2.2"
        }
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

        object Kotlinx {
            const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
            const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinDateTime}"
            const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0"
        }
    }

    object Ktor {
        private const val VERSION = "2.1.1"

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
            const val darwin = "io.ktor:ktor-client-darwin:$VERSION"
        }

        object Serialization {
            const val json = "io.ktor:ktor-serialization-kotlinx-json:$VERSION"
        }
    }

    object Test {
        const val junit = "junit:junit:${Versions.junit}"
        const val androidXTestJUnit = "androidx.test.ext:junit:${Versions.androidXTestJUnit}"
        const val mockito = "org.mockito:mockito-inline:${Versions.mockito}"
        const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
        const val testCore = "androidx.test:core:${Versions.testCore}"
    }
}