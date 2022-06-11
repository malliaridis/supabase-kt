package io.supabase.postgrest

import io.ktor.client.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.supabase.postgrest.builder.Count
import io.supabase.postgrest.builder.PostgrestBuilder
import io.supabase.postgrest.builder.PostgrestQueryBuilder
import io.supabase.postgrest.builder.PostgrestRpcBuilder
import kotlinx.serialization.Serializable

/**
 * Main client and entry point for using PostgREST client.
 *
 * @param[url] URL of the PostgREST endpoint.
 * @param[headers] Custom headers.
 * @param[schema] Postgres schema to switch to.
 * @param[httpClient] HttpClient to use for requests.
 */
open class PostgrestClient(
    private val url: String,
    private var headers: Headers = headersOf(),
    private val schema: String = "public",
    private val httpClient: () -> HttpClient
) {

    /**
     * Authenticates the request with JWT.
     *
     * @param token  The JWT token to use.
     */
    fun auth(token: String): PostgrestClient {
        headers = buildHeaders {
            appendAll(headers)
            append("Authorization", "Bearer $token")
        }
        return this
    }

    /**
     * Perform a table operation.
     *
     * @param[table] The table name to operate on.
     */
    fun <T : @Serializable Any> from(table: String): PostgrestQueryBuilder<T> {
        return PostgrestQueryBuilder("$url/$table", headers, schema, httpClient)
    }

    /**
     * Perform a stored procedure call.
     *
     * @param[fn] The function name to call.
     * @param[params] The parameters to pass to the function call.
     * TODO Change params to Serializable and parse to key-value
     */
    fun <T : @Serializable Any> rpc(fn: String, params: Any?, head: Boolean?, count: Count?): PostgrestBuilder<T> {
        return PostgrestRpcBuilder<T>("$url/rpc/$fn", headers, schema, httpClient).rpc(params, head, count)
    }
}