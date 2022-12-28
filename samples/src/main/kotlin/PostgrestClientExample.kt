import io.supabase.SupabaseClient
import io.supabase.postgrest.http.PostgrestHttpResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

fun main() = runBlocking {
    val supabaseUrl = System.getenv("SUPABASE_URL")
    val supabaseApiKey = System.getenv("SUPABASE_API_KEY")

    val client = SupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseApiKey,
    )
    val response = client.from<Todo>("todos")
        .select("id,text")
        .order("text")
        .execute<List<Todo>>()

    when (response) {
        is PostgrestHttpResponse.Failure -> println(response.error)
        is PostgrestHttpResponse.Success -> println(response.body)
    }
}

@Serializable
private data class Todo(
    val id: Int,
    val text: String
)