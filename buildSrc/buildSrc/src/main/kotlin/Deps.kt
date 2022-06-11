object Versions {
    const val androidMinSdk = 21
    const val androidCompileSdk = 31
    const val androidTargetSdk = androidCompileSdk

    const val jvmTarget = "11"

    const val kotlin = "1.6.21"
    const val kotlinCoroutines = "1.6.1"
    const val ktor = "2.0.2"
    const val kotlinxSerialization = "1.3.2" // 1.3.3
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
            const val androidBuildTools = "com.android.tools.build:gradle:7.0.4"
            const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
            const val serialization = "org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}"
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
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
            const val testCommon = "org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlin}"
            const val testJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
            const val testJs = "org.jetbrains.kotlin:kotlin-test-js:${Versions.kotlin}"
            const val testAnnotationsCommon = "org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlin}"
        }

        object Kotlinx {
            const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
            const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinDateTime}"
        }
    }

    object Ktor {
        object Client {
            const val core = "io.ktor:ktor-client-core:${Versions.ktor}"
            const val auth = "io.ktor:ktor-client-auth:${Versions.ktor}"
            // TODO See if this dependency is used at all
            const val json = "io.ktor:ktor-client-json:${Versions.ktor}"
            const val logging = "io.ktor:ktor-client-logging:${Versions.ktor}"
            const val contentNegotiation = "io.ktor:ktor-client-content-negotiation:${Versions.ktor}"
            const val websockets = "io.ktor:ktor-client-websockets:${Versions.ktor}"

            // Engines
            const val android = "io.ktor:ktor-client-android:${Versions.ktor}"
            const val java = "io.ktor:ktor-client-java:${Versions.ktor}"
            const val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
            const val js = "io.ktor:ktor-client-js:${Versions.ktor}"
            const val cio = "io.ktor:ktor-client-cio:${Versions.ktor}"
            const val darwin = "io.ktor:ktor-client-darwin:${Versions.ktor}"
        }

        object Serialization {
            const val json = "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}"
        }
    }

    object Test {
        const val junit = "junit:junit:${Versions.junit}"
        const val androidXTestJUnit = "androidx.test.ext:junit:${Versions.androidXTestJUnit}"
        const val mockito = "org.mockito:mockito-inline:${Versions.mockito}"
        const val ktorMock = "io.ktor:ktor-client-mock:${Versions.ktor}"
        const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
        const val testCore = "androidx.test:core:${Versions.testCore}"
    }
}