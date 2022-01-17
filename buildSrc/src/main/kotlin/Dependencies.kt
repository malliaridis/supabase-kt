object Versions {
    const val androidMinSdk = 21
    const val androidCompileSdk = 31
    const val androidTargetSdk = androidCompileSdk

    const val jvmTarget = "11"

    const val kotlinCoroutines = "1.6.0-RC3"
    const val ktor = "1.6.5"
    const val kotlinxSerialization = "1.3.0"
    const val kotlinDateTime = "0.3.1"

    const val junit = "4.12"
    const val androidXTestJUnit = "1.1.3"
    const val testCore = "1.3.0"
    const val mockito = "3.11.2"
    const val robolectric = "4.6.1"

    const val lifecycleKtx = "2.4.0-rc01"
    const val lifecycleRuntimeKtx = lifecycleKtx
    const val lifecycleViewmodelKtx = lifecycleKtx
}

object Deps {

    object Kotlinx {
        const val serializationCore =
            "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinDateTime}"
    }

    object Test {
        const val junit = "junit:junit:${Versions.junit}"
        const val androidXTestJUnit = "androidx.test.ext:junit:${Versions.androidXTestJUnit}"
        const val mockito = "org.mockito:mockito-inline:${Versions.mockito}"
        const val ktorMock = "io.ktor:ktor-client-mock:${Versions.ktor}"
        const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
        const val testCore = "androidx.test:core:${Versions.testCore}"
    }

    object KtorClient {
        const val core = "io.ktor:ktor-client-core:${Versions.ktor}"
        const val auth = "io.ktor:ktor-client-auth:${Versions.ktor}"
        const val json = "io.ktor:ktor-client-json:${Versions.ktor}"
        const val logging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        const val serialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"
        const val websockets = "io.ktor:ktor-client-websockets:${Versions.ktor}"
        const val android = "io.ktor:ktor-client-android:${Versions.ktor}"
        const val java = "io.ktor:ktor-client-java:${Versions.ktor}"
        const val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
        const val js = "io.ktor:ktor-client-js:${Versions.ktor}"
        const val cio = "io.ktor:ktor-client-cio:${Versions.ktor}"
    }
}
