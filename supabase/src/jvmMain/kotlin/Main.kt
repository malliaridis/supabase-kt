import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.supabase.SupabaseClient
import io.supabase.gotrue.http.results.SessionResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

fun main() {
    runBlocking {

        val httpClient = HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }
        }
        val supabase = SupabaseClient(
            supabaseUrl = System.getenv("SUPABASE_URL"),
            supabaseKey = System.getenv("SUPABASE_API_KEY"),
            httpClient = { httpClient }
        )
        val email = System.getenv("SUPABASE_USERNAME")
        val password = System.getenv("SUPABASE_PASSWORD")

        when (val result = supabase.auth.signIn(email = email, password = password)) {
            is SessionResult.Success -> {
                println("Hello, ${result.data.user}!")
                println("Your data is ${result.data.user?.userMetadata} and your role is ${result.data.user?.role}.")
                val r = supabase.from<Todo>("todos").select().eq("id", 2).executeAndGetList<Todo>()
                println("You got ${r.size} elements.")
                println("Content is ${r[0]}")
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

@Serializable
private data class Todo(
    val id: Int,
    val text: String
)