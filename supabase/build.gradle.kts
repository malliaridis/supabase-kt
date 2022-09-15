plugins {
    id("multiplatform-library")
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
