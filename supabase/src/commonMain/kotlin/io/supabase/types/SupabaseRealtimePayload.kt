package io.supabase.types

data class SupabaseRealtimePayload<T>(

    val commit_timestamp: String,

    val eventType: SupabaseEventTypes,

    val schema: String,

    val table: String,

    /**
     * The new record. Present for 'INSERT' and 'UPDATE' events.
     */
    val new: T,

    /**
     * The previous record. Present for 'UPDATE' and 'DELETE' events.
     */
    val old: T
)

data class SimplePayload<T>(

    /**
     * The new record. Present for 'INSERT' and 'UPDATE' events.
     */
    val new: T,

    /**
     * The previous record. Present for 'UPDATE' and 'DELETE' events.
     */
    val old: T
)