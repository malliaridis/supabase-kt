package io.supabase

import io.ktor.http.*
import io.supabase.realtime.RealtimeClient
import io.supabase.realtime.RealtimeSubscription
import io.supabase.types.SimplePayload
import io.supabase.types.SupabaseEventTypes
import io.supabase.types.SupabaseRealtimePayload

class SupabaseRealtimeClient(
    socket: RealtimeClient,
    headers: Headers,
    schema: String,
    tableName: String
) {

    private val userToken: String? = headers["Authorization"]?.split(" ")?.getOrNull(1)
    private val chanParams: Map<String, String> =
        if (userToken != null) mapOf("user_token" to userToken) else emptyMap()
    private val topic = if (tableName == "*") "realtime:$schema" else "realtime:$schema:$tableName"

    val subscription: RealtimeSubscription = socket.channel(topic, chanParams)

    private fun <T> getPayloadRecords(payload: SupabaseRealtimePayload<T?>): SimplePayload<T?> = SimplePayload(
        new = if (payload.eventType.isInsertOrUpdate) payload.new else null,
        old = if (payload.eventType.isUpdateOrDelete) payload.old else null
    )

    /**
     * The event you want to listen to.
     *
     * @param event The event
     * @param callback A callback function that is called whenever the event occurs.
     */
    fun <T> on(
        event: SupabaseEventTypes,
        callback: (payload: SupabaseRealtimePayload<T?>) -> Unit
    ): SupabaseRealtimeClient {
        subscription.on(event.toString()) { payload: Any?, _ ->
            if (payload is SupabaseRealtimePayload<*>) try {
                // TODO See if this is sufficient
                callback(payload as SupabaseRealtimePayload<T?>)
            } catch (exception: Exception) {
                callback(
                    SupabaseRealtimePayload(
                        schema = payload.schema,
                        table = payload.table,
                        commitTimestamp = payload.commitTimestamp,
                        eventType = payload.eventType,
                    )
                )
            }
        }
        return this
    }

    /**
     * Enables the subscription.
     */
    fun subscribe(callback: (event: String, error: Any?) -> RealtimeSubscription): RealtimeSubscription {
        subscription.onError { e: Error -> callback("SUBSCRIPTION_ERROR", e) }
        subscription.onClose { _, _ -> callback("CLOSED", null) }
        subscription
            .subscribe()
            .receive("ok") { callback("SUBSCRIBED", null) }
            .receive("error") { error: Any? -> callback("SUBSCRIPTION_ERROR", error) }
            .receive("timeout") { callback("RETRYING_AFTER_TIMEOUT", null) }
        return subscription
    }
}
