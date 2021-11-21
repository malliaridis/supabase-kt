package io.supabase.postgrest

import io.ktor.http.*
import io.supabase.postgrest.builder.Count
import io.supabase.postgrest.builder.PostgrestBuilder
import io.supabase.postgrest.builder.PostgrestQueryBuilder
import io.supabase.postgrest.builder.PostgrestRpcBuilder
import io.supabase.postgrest.http.PostgrestHttpClient
import kotlinx.serialization.Serializable

/**
 * Main client and entry point for using PostgREST client.
 *
 * @param[url] URL of the PostgREST endpoint.
 * @param[headers] Custom headers.
 * @param[schema] Postgres schema to switch to.
 * @param[httpClient] Implementation of the [PostgrestHttpClient] interface.
 */
open class PostgrestClient(
    private val url: Url,
    private val headers: MutableMap<String, String> = mutableMapOf(),
    private val schema: String? = null,
    val httpClient: PostgrestHttpClient
) {

    /**
     * Authenticates the request with JWT.
     *
     * @param token  The JWT token to use.
     */
    fun auth(token: String): PostgrestClient {
        this.headers["Authorization"] = "Bearer $token"
        return this
    }

    /**
     * Perform a table operation.
     *
     * @param[table] The table name to operate on.
     */
    fun <T : @Serializable Any> from(table: String): PostgrestQueryBuilder<T> {
        val url = Url("$url/$table")
        return PostgrestQueryBuilder(url, httpClient, headers, schema)
    }

    /**
     * Perform a stored procedure call.
     *
     * @param[fn] The function name to call.
     * @param[params] The parameters to pass to the function call.
     */
    fun <T : @Serializable Any> rpc(fn: String, params: Any?, head: Boolean?, count: Count?): PostgrestBuilder<T> {
        val url = Url("${this.url}/rpc/${fn}")

        return PostgrestRpcBuilder<T>(url, httpClient, headers, schema).rpc(params, head, count)
    }
}