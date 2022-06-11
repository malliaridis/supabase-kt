package io.supabase.postgrest.builder

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class PostgrestRpcBuilder<T : @Serializable Any>(
    url: String,
    headers: Headers = headersOf(),
    schema: String = "public",
    httpClient: () -> HttpClient,
) : PostgrestBuilder<T>(url, headers, schema, httpClient) {

    /**
     * Perform a function call.
     * TODO Document parameters
     * TODO Change params to Serializable and parse to key-value
     */
    fun rpc(
        params: Any?,
        head: Boolean? = false,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        if (head == true) {
            method = HttpMethod.Head

            if (params != null && params is Map<*, *>) {
                // TODO Change Map<String, String> to Map<String, Serializable>
                (params as? Map<String, String>)?.forEach { (key, value) ->
                    setSearchParam(key, value)
                }
            }
        } else {
            method = HttpMethod.Post
            body = params
        }

        if (count != null) {
            setHeader("count", "$count")
        }

        return PostgrestFilterBuilder(this)
    }

    /**
     * TODO See if this function is necessary.
     * // TODO Change params to Serializable
     */
    internal fun rpc(params: Any?): PostgrestBuilder<T> {
        method = HttpMethod.Post
        body = params
        return this
    }
}
