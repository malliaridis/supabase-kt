package io.supabase.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseRealtimePayload<T>(

    @SerialName("commit_timestamp") val commitTimestamp: String,

    @SerialName("type") val eventType: SupabaseEventTypes,

    val schema: String,

    val table: String,

    /**
     * The new record. Present for 'INSERT' and 'UPDATE' events.
     */
    @SerialName("record") val new: T? = null,

    /**
     * The previous record. Present for 'UPDATE' and 'DELETE' events.
     */
    @SerialName("old_record") val old: T? = null,
)

data class SimplePayload<T>(

    /**
     * The new record. Present for 'INSERT' and 'UPDATE' events.
     */
    val new: T?,

    /**
     * The previous record. Present for 'UPDATE' and 'DELETE' events.
     */
    val old: T?
)