pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "supabase-kt"

include(":gotrue")
include(":postgrest")
include(":realtime")
include(":storage")
include(":supabase")
include(":samples")
