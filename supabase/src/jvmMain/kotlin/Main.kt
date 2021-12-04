import io.ktor.client.*
import io.ktor.client.features.json.*
import io.supabase.SupabaseClient
import io.supabase.gotrue.http.results.SessionResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject

fun main() {
    runBlocking {

        val httpClient = HttpClient { install(JsonFeature) }
        val supabase = SupabaseClient(
            supabaseUrl = System.getenv("SUPABASE_URL"),
            supabaseKey = System.getenv("SUPABASE_API_KEY"),
            httpClient = { httpClient }
        )

        when (val result = supabase.auth.signIn(
            email = System.getenv("SUPABASE_USERNAME"),
            password = System.getenv("SUPABASE_PASSWORD")
        )
        ) {
            is SessionResult.Success -> {
                println("Hello, ${result.data.user}!")
                println("Your data is ${result.data.user?.userMetadata} and your role is ${result.data.user?.role}.")
                val r = supabase.from<JsonObject>("todos").select().executeAndGetList<JsonObject>()
                println("You got ${r.size} elements.")
            }
            is SessionResult.Failure -> {
                println("An error occurred.")
                println("${result.error.status}: ${result.error.message}")
            }
        }

        supabase.auth.signOut()

        if (supabase.auth.user() != null) println("You should be signed out.")
        else println("You are signed out, yeah")
    }
}