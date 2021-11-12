package io.supabase.postgrest.http

import io.ktor.http.*

/**
 * Interface used by the PostgrestClient, allows replacing the default HTTP client.
 *
 * Overwrite it to replace the default Apache HTTP Client implementation.
 */
interface PostgrestHttpClient {

    suspend fun execute(
        uri: Url,
        method: HttpMethod,
        headers: Map<String, String> = emptyMap(),
        body: Any? = null
    ): PostgrestHttpResponse
}