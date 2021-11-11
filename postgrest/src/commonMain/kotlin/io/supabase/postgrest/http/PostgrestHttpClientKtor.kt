package io.supabase.postgrest.http

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Default implementation of the [PostgrestHttpClient] used by the PostgrestDefaultClient.
 *
 * Uses closable apache HTTP-Client 5.x.
 */
class PostgrestHttpClientKtor(
    private val httpClient: () -> HttpClient
) : PostgrestHttpClient {

    override suspend fun execute(
        uri: Url,
        method: HttpMethod,
        headers: Map<String, String>,
        body: @Serializable Any?
    ): PostgrestHttpResponse {
        return httpClient().request(uri) {
            this.method = method
            this.body = Json.encodeToString(body)
            this.headers {
                headers.forEach { (name, value) -> append(name, value) }
            }
        }
    }
}