plugins {
    id("android-setup")
    id("multiplatform-setup")
    id("ktor-setup")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(Deps.Ktor.Client.websockets)
            }
        }
    }
}
