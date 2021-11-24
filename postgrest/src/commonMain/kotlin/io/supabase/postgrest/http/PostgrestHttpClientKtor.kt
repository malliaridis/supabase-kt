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
 * Uses Ktor HTTP client.
 */
class PostgrestHttpClientKtor(
    private val httpClient: () -> HttpClient
) : PostgrestHttpClient {

    override suspend fun <R : @Serializable Any> execute(
        uri: Url,
        method: HttpMethod,
        headers: Headers,
        body: @Serializable Any?
    ): PostgrestHttpResponse<R> {
        return httpClient().request(uri) {
            this.method = method
            this.body = Json.encodeToString(body)
            this.headers { appendAll(headers) }
        }
    }
}