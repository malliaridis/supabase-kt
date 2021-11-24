package io.supabase

import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.supabase.builder.SupabaseQueryBuilder
import io.supabase.gotrue.GoTrueClient
import io.supabase.http.SupabaseAuthClient
import io.supabase.http.SupabaseHttpClient
import io.supabase.postgrest.PostgrestClient
import io.supabase.postgrest.PostgrestDefaultClient
import io.supabase.postgrest.builder.Count
import io.supabase.postgrest.builder.PostgrestBuilder
import io.supabase.realtime.RealtimeClient
import io.supabase.realtime.RealtimeDefaultClient
import io.supabase.realtime.RealtimeSubscription
import io.supabase.realtime.helper.DEFAULT_HEADERS
import io.supabase.storage.StorageClient
import io.supabase.storage.StorageDefaultClient
import io.supabase.types.SupabaseClientOptions

val DEFAULT_OPTIONS = SupabaseClientOptions(
    schema = "public",
    headers = emptyMap(),
    autoRefreshToken = true,
    persistSession = true,
    detectSessionInUrl = true,
    localStorage = null,
    realtime = null
)

/**
 *  Main client and entry point for using Supabase client.
 *
 * @param supabaseUrl The unique Supabase URL which is supplied when you create a new project in your project dashboard.
 * @param supabaseKey The unique Supabase Key which is supplied when you create a new project in your project dashboard.
 */
open class SupabaseClient(
    private val supabaseUrl: Url,
    /**
     * The unique Supabase Key which is supplied when you create a new project in your project dashboard.
     */
    private val supabaseKey: String,
    private val options: SupabaseClientOptions?
) {

    private val restUrl = "${supabaseUrl}/rest/v1"
    private val realtimeUrl = "${supabaseUrl}/realtime/v1".replace("http", "ws")
    private val authUrl = "${supabaseUrl}/auth/v1"
    private val storageUrl = "${supabaseUrl}/storage/v1"

    private val settings = SupabaseClientOptions(
        schema = options?.schema ?: DEFAULT_OPTIONS.schema,
        headers = options?.headers ?: DEFAULT_OPTIONS.headers,
        autoRefreshToken = options?.autoRefreshToken ?: DEFAULT_OPTIONS.autoRefreshToken,
        persistSession = options?.persistSession ?: DEFAULT_OPTIONS.persistSession,
        detectSessionInUrl = options?.detectSessionInUrl ?: DEFAULT_OPTIONS.detectSessionInUrl,
        localStorage = options?.localStorage ?: DEFAULT_OPTIONS.localStorage,
        realtime = options?.realtime ?: DEFAULT_OPTIONS.realtime
    )

    /**
     * Supabase Auth allows you to create and manage user sessions for access to data that is secured by access policies.
     */
    private val auth: SupabaseAuthClient = initSupabaseAuthClient()

    private val realtime: RealtimeClient = initRealtimeClient()

    /**
     * Supabase Storage allows you to manage user-generated content, such as photos or videos.
     * TODO Replace fetch with httpClient
     */
    private val storage: StorageClient = initStorageClient()

    private val postgrest: PostgrestClient = initPostgRESTClient()

    private val httpClient: SupabaseHttpClient = TODO()

    /**
     * Perform a table operation.
     *
     * @param table The table name to operate on.
     */
    fun <T : Any> from(table: String): SupabaseQueryBuilder<T> {
        return SupabaseQueryBuilder(
            url = Url("${restUrl}/${table}"),
            headers = getAuthHeaders(),
            schema = settings.schema,
            postgrest = postgrest.httpClient,
            realtime = realtime,
            table = table
        )
    }

    /**
     * Perform a function call.
     *
     * @param fn  The function name to call.
     * @param params  The parameters to pass to the function call.
     * @param head   When set to true, no data will be returned.
     * @param count  Count algorithm to use to count rows in a table.
     *
     */
    fun <T : Any> rpc(
        fn: String,
        params: Any?,
        head: Boolean = false,
        count: Count? = null
    ): PostgrestBuilder<T> {
        // TODO See if same client should be returned or always a new one should be created
//        val rest = initPostgRESTClient()
//        return rest.rpc(fn, params, head, count)
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
            append("Authorization", "Bearer $supabaseKey")
            append("apikey", supabaseKey)
            settings.headers?.forEach { header, value ->
                appendAll(header, value)
            }
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
        )
    }

    private fun initStorageClient(): StorageClient {
        return StorageDefaultClient(storageUrl, getAuthHeaders())
    }

    private fun initRealtimeClient(): RealtimeClient {
        return RealtimeDefaultClient(
            url = Url(realtimeUrl),
            options = settings.realtime,
            apiKey = supabaseKey
        )
    }

    private fun initPostgRESTClient(): PostgrestClient {
        return PostgrestDefaultClient(
            url = Url(restUrl),
            headers = getAuthHeaders(),
            schema = settings.schema
        )
    }

    private fun getAuthHeaders(): Map<String, String> {
        val headers: MutableMap<String, String> = DEFAULT_HEADERS.toMutableMap()
        val authBearer = auth.session()?.accessToken ?: supabaseKey
        headers["apikey"] = supabaseKey
        headers["Authorization"] = "Bearer $authBearer"
        return headers
    }

    private suspend fun closeChannel(subscription: RealtimeSubscription) {
        subscription.unsubscribe()
        realtime.remove(subscription)
    }
}
