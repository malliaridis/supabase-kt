package io.supabase.types

import io.ktor.http.*
import io.supabase.gotrue.types.GoTrueClientOptions
import io.supabase.gotrue.types.SupportedStorage
import io.supabase.realtime.RealtimeClientOptions

// TODO See if this is used somewhere else
typealias SupabaseAuthClientOptions = GoTrueClientOptions

data class SupabaseClientOptions(

    /**
     * The Postgres schema which your tables belong to. Must be on the list of exposed schemas in Supabase. Defaults to 'public'.
     */
    val schema: String?,

    /**
     * Optional headers for initializing the client.
     */
    val headers: Headers?,

    /**
     * Automatically refreshes the token for logged in users.
     */
    val autoRefreshToken: Boolean?,

    /**
     * Whether to persist a logged in session to storage.
     */
    val persistSession: Boolean?,

    /**
     * Detect a session from the URL. Used for OAuth login callbacks.
     */
    val detectSessionInUrl: Boolean?,

    /**
     * A storage provider. Used to store the logged in session.
     */
    val localStorage: SupportedStorage?,

    /**
     * Options passed to the realtime-js instance
     */
    val realtime: RealtimeClientOptions?,

    /**
     * A custom `fetch` implementation.
     * TODO Replace with httpClient instance
     */
    //    fetch?: Fetch
)
