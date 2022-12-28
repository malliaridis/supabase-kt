import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.http.results.SessionResult
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val goTrueUrl = "${System.getenv("SUPABASE_URL")}/auth/v1"
        val apiKey = System.getenv("SUPABASE_API_KEY")

        val headers = headersOf("apikey", apiKey)
        val httpClient = HttpClient(CIO)
        val client = GoTrueClient(url = goTrueUrl, headers = headers, httpClient = { httpClient })

        when (val result =
            client.signIn(email = System.getenv("SUPABASE_USERNAME"), password = System.getenv("SUPABASE_PASSWORD"))) {
            is SessionResult.Success -> {
                println("Hello, ${result.data.user}!")
                println("Your data is ${result.data.user?.userMetadata} and your role is ${result.data.user?.role}.")
            }
            is SessionResult.Failure -> {
                println("An error occurred.")
                println("${result.error.status}: ${result.error.message}")
            }
        }

        client.signOut()

        if (client.user() != null) println("You should be signed out.")
        else println("You are signed out, yeah")
    }
}
