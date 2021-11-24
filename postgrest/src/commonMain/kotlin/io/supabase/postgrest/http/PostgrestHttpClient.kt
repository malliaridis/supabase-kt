package io.supabase.postgrest.http

import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Interface used by the PostgrestClient, allows replacing the default HTTP client.
 *
 * Overwrite it to replace the default Apache HTTP Client implementation.
 */
interface PostgrestHttpClient {

    suspend fun <T : @Serializable Any> execute(
        uri: Url,
        method: HttpMethod,
        headers: Headers = headersOf(),
        body: Any? = null
    ): PostgrestHttpResponse<T>
}
