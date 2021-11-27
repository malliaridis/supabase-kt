package io.supabase.builder

import io.ktor.client.*
import io.ktor.http.*
import io.supabase.SupabaseRealtimeClient
import io.supabase.postgrest.builder.PostgrestQueryBuilder
import io.supabase.realtime.RealtimeClient
import io.supabase.types.SupabaseEventTypes
import io.supabase.types.SupabaseRealtimePayload

class SupabaseQueryBuilder<T : Any>(
    url: String,
    headers: Headers,
    schema: String = "public",
    table: String,
    val realtime: RealtimeClient,
    httpClient: () -> HttpClient
) : PostgrestQueryBuilder<T>(url, headers, schema, httpClient) {

    private val subscription = SupabaseRealtimeClient(realtime, headers, schema, table)

    /**
     * Subscribe to realtime changes in your databse.
     * @param event The database event which you would like to receive updates for, or you can use the special wildcard `*` to listen to all changes.
     * @param callback A callback that will handle the payload that is sent whenever your database changes.
     */
    fun on(event: SupabaseEventTypes, callback: (payload: SupabaseRealtimePayload<*>) -> Unit): SupabaseRealtimeClient {
        if (!realtime.isConnected()) {
            realtime.connect()
        }
        return subscription.on(event, callback)
    }
}