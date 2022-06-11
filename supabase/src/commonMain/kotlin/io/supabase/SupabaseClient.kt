package io.supabase

import io.ktor.client.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.supabase.builder.SupabaseQueryBuilder
import io.supabase.gotrue.GoTrueClient
import io.supabase.gotrue.types.SupportedStorage
import io.supabase.http.SupabaseAuthClient
import io.supabase.postgrest.PostgrestClient
import io.supabase.postgrest.builder.Count
import io.supabase.postgrest.builder.PostgrestBuilder
import io.supabase.realtime.RealtimeClient
import io.supabase.realtime.RealtimeClientOptions
import io.supabase.realtime.RealtimeSubscription
import io.supabase.realtime.helper.DEFAULT_HEADERS
import io.supabase.storage.StorageClient
import kotlinx.serialization.Serializable

/**
 *  Main client and entry point for using Supabase client.
 *
 * @param supabaseUrl The unique Supabase URL which is supplied when you create a new project in your project dashboard.
 * @param supabaseKey The unique Supabase Key which is supplied when you create a new project in your project dashboard.
 */
open class SupabaseClient(
    private val supabaseUrl: String,

    /**
     * The unique Supabase Key which is supplied when you create a new project in your project dashboard.
     */
    private val supabaseKey: String,

    /**
     * The Postgres schema which your tables belong to. Must be on the list of exposed schemas in Supabase. Defaults to 'public'.
     */
    private val schema: String = "public",

    /**
     * Optional headers for initializing the client.
     */
    private val headers: Headers = headersOf(),

    /**
     * Automatically refreshes the token for logged in users.
     */
    private val autoRefreshToken: Boolean = true,

    /**
     * Whether to persist a logged-in session to storage.
     */
    private val persistSession: Boolean = true,

    /**
     * Detect a session from the URL. Used for OAuth login callbacks.
     */
    private val detectSessionInUrl: Boolean = true,

    /**
     * A storage provider. Used to store the logged-in session.
     */
    private val localStorage: SupportedStorage? = null,

    /**
     * Options passed to the realtime instance
     */
    private val realtimeOptions: RealtimeClientOptions? = null,

    /**
     * HttpClient to use for requests.
     */
    private val httpClient: () -> HttpClient = { HttpClient() }
) {

    private val restUrl = "$supabaseUrl/rest/v1"
    private val realtimeUrl = "$supabaseUrl/realtime/v1".replace("http", "ws")
    private val authUrl = "$supabaseUrl/auth/v1"
    private val storageUrl = "$supabaseUrl/storage/v1"

    /**
     * Supabase Auth allows you to create and manage user sessions for access to data that is secured by access policies.
     */
    val auth: SupabaseAuthClient = initSupabaseAuthClient()

    private val realtime: RealtimeClient = initRealtimeClient()

    /**
     * Supabase Storage allows you to manage user-generated content, such as photos or videos.
     */
    val storage: StorageClient = initStorageClient()

    private val postgrest: PostgrestClient = initPostgRESTClient()

    init {
        if (supabaseUrl.isBlank()) throw IllegalArgumentException("supabaseUrl should not be empty.")
        if (supabaseKey.isBlank()) throw IllegalArgumentException("supabaseKey should not be empty.")
        // TODO Add URL validation
        // TODO Check if supabase URL ends with slash (URLs are concatenated with slash and would cause double slash)
    }

    /**
     * Perform a table operation.
     *
     * @param table The table name to operate on.
     */
    fun <T : Any> from(table: String): SupabaseQueryBuilder<T> {
        return SupabaseQueryBuilder(
            url = "${restUrl}/$table",
            headers = getAuthHeaders(),
            schema = schema,
            table = table,
            realtime = realtime,
            httpClient = httpClient
        )
    }

    /**
     * Perform a function call.
     *
     * @param fn  The function name to call.
     * @param params  The parameters to pass to the function call.
     * @param head   When set to true, no data will be returned.
     * @param count  Count algorithm to use to count rows in a table.
     * TODO Change params to Serializable and parse to key-value
     */
    fun <T : @Serializable Any> rpc(
        fn: String,
        params: Any?,
        head: Boolean = false,
        count: Count? = null
    ): PostgrestBuilder<T> {
        return postgrest.rpc(fn, params, head, count)
    }

    /**
     * Removes an active subscription and returns the number of open connections.
     *
     * @param subscription The subscription you want to remove.
     */
    suspend fun removeSubscription(subscription: RealtimeSubscription): Int {
        closeSubscription(subscription)

        val openSubscriptions = getSubscriptions().size
        if (openSubscriptions > 0) realtime.disconnect(CloseReason.Codes.NORMAL, null)
        return openSubscriptions
    }

    private suspend fun closeSubscription(subscription: RealtimeSubscription) {
        if (!subscription.isClosed()) {
            closeChannel(subscription)
        }
    }

    /**
     * Returns an array of all your subscriptions.
     */
    fun getSubscriptions(): List<RealtimeSubscription> {
        return realtime.channels
    }

    private fun initSupabaseAuthClient(): SupabaseAuthClient {

        val authHeaders = buildHeaders {
            append("apikey", supabaseKey)
            appendAll(this@SupabaseClient.headers)
        }

        return GoTrueClient(
            url = authUrl,
            headers = authHeaders,
            // TODO See if the below data need to be passed
//            autoRefreshToken = autoRefreshToken,
//            persistSession = persistSession,
//            detectSessionInUrl = detectSessionInUrl,
//            localStorage = localStorage,
            // fetch: this.fetch, // TODO See if there is a client necessary to pass
            httpClient = httpClient
        )
    }

    private fun initStorageClient() = StorageClient(storageUrl, getAuthHeaders(), httpClient)

    private fun initRealtimeClient() = RealtimeClient(realtimeUrl, realtimeOptions)

    private fun initPostgRESTClient() = PostgrestClient(restUrl, getAuthHeaders(), schema, httpClient)

    private fun getAuthHeaders(): Headers {
        val authBearer = auth.session()?.accessToken ?: supabaseKey
        val headers = buildHeaders {
            appendAll(DEFAULT_HEADERS)
            append("apikey", supabaseKey)
            append("Authorization", "Bearer $authBearer")
        }
        return headers
    }

    private fun closeChannel(subscription: RealtimeSubscription) {
        subscription.unsubscribe()
        realtime.remove(subscription)
    }
}
