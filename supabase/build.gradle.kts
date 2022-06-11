plugins {
    id("android-setup")
    id("multiplatform-setup")
    id("ktor-setup")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":gotrue"))
                implementation(project(":postgrest"))
                implementation(project(":realtime"))
                implementation(project(":storage"))
            }
        }
    }
}
