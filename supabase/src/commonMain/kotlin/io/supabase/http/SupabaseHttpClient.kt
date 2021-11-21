package io.supabase.http

/**
 * Interface used by the PostgrestClient, allows replacing the default HTTP client.
 *
 * Overwrite it to replace the default Apache HTTP Client implementation.
 */
interface SupabaseHttpClient {

//    suspend fun <T : @Serializable Any> execute(
//        uri: Url,
//        method: HttpMethod,
//        headers: Map<String, String> = emptyMap(),
//        body: Any? = null
//    ): PostgrestHttpResponse<T>
}
