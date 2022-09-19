plugins {
    id("multiplatform-library")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(project(":gotrue"))
                api(project(":postgrest"))
                api(project(":realtime"))
                api(project(":storage"))
            }
        }
    }
}
