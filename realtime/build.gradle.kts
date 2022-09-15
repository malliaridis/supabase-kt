plugins {
    id("multiplatform-library")
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
