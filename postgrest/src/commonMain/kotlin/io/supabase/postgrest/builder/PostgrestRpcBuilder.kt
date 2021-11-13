package io.supabase.postgrest.builder

import io.ktor.http.*
import io.supabase.postgrest.http.PostgrestHttpClient
import kotlinx.serialization.Serializable

class PostgrestRpcBuilder<T : @Serializable Any>(
    url: Url,
    postgrestHttpClient: PostgrestHttpClient,
    defaultHeaders: Map<String, String>,
    schema: String?
) : PostgrestBuilder<T>(url, postgrestHttpClient, defaultHeaders, schema) {

    /**
     * Perform a function call.
     */
    fun rpc(
        params: Any?,
        head: Boolean = false,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        if (head) {
            setMethod(HttpMethod.Head)

            if (params != null && params is Map<*, *>) {
                (params as? Map<String, String>)?.forEach { (key, value) ->
                    setSearchParam(key, value)
                }
            }
        } else {
            setMethod(HttpMethod.Post)
            setBody(params)
        }

        if (count != null) {
            setHeader("count", "$count")
        }

        return PostgrestFilterBuilder(this)
    }

    /**
     * TODO See if this function is necessary.
     */
    internal fun rpc(params: Any?): PostgrestBuilder<T> {
        setMethod(HttpMethod.Post)
        setBody(params)
        return this
    }
}
