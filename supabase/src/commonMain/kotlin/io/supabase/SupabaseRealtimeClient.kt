package io.supabase

import io.supabase.realtime.RealtimeClient
import io.supabase.realtime.RealtimeSubscription
import io.supabase.types.SimplePayload
import io.supabase.types.SupabaseEventTypes
import io.supabase.types.SupabaseRealtimePayload

class SupabaseRealtimeClient(
    private val socket: RealtimeClient,
    private val headers: Map<String, String>,
    private val schema: String?,
    private val tableName: String
) {


    val chanParams: MutableMap<String, String> = mutableMapOf()
    val topic = if (tableName == "*") "realtime:$schema" else "realtime:$schema:$tableName"
    private val userToken = headers["Authorization"]?.split(" ")?.get(1)

    val subscription: RealtimeSubscription = socket.channel(topic, chanParams)

    init {
        if (userToken != null) {
            chanParams["user_token"] = userToken
        }
    }

    // TODO Replace with type casting and handling of INSERT, UPDATE, DELETE payloads
    private fun <T> getPayloadRecords(payload: SupabaseRealtimePayload<T>): SimplePayload<T>? {
//        val records = {
//            new: {},
//            old: {},
//        }
//
//        if (payload.type == "INSERT" || payload.type == "UPDATE") {
//            records.new = Transformers.convertChangeData(payload.columns, payload.record)
//        }
//
//        if (payload.type == "UPDATE" || payload.type == "DELETE") {
//            records.old = Transformers.convertChangeData(payload.columns, payload.old_record)
//        }

        return null
    }

    /**
     * The event you want to listen to.
     *
     * @param event The event
     * @param callback A callback function that is called whenever the event occurs.
     */
    fun on(
        event: SupabaseEventTypes,
        callback: (payload: SupabaseRealtimePayload<Any>) -> Unit
    ): SupabaseRealtimeClient {
        subscription.on(event.toString()) { payload: Any?, _ ->
            if (payload is SupabaseRealtimePayload<*>) {

                var enrichedPayload: SupabaseRealtimePayload<Any> = SupabaseRealtimePayload(
                    schema = payload.schema,
                    table = payload.table,
                    commit_timestamp = payload.commit_timestamp,
                    eventType = payload.eventType,
                    new = {},
                    old = {},
                )

                // TODO Set new and old payload data by parsing them properly
//                enrichedPayload = { ...enrichedPayload, ...getPayloadRecords(payload) }

                callback(enrichedPayload)
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
