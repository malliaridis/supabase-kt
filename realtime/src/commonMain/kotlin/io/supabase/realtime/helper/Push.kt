package io.supabase.realtime.helper

import io.supabase.realtime.Message
import io.supabase.realtime.RealtimeSubscription

data class Hook(
    val status: String,
    val callback: (any: Any?) -> Unit
)

data class ReceivedResponse(
    val status: String,
    val response: (any: Any?) -> Unit
)

/**
 * @param channel The Channel
 * @param event The event, for example `"phx_join"`
 * @param payload The payload, for example `{user_id: 123}`
 * @param timeout The push timeout in milliseconds
 */
class Push(
    val channel: RealtimeSubscription,
    val event: ChannelEvent,
    val payload: Any? = null,
    var timeout: Long = DEFAULT_TIMEOUT
) {

    var sent: Boolean = false

    var timeoutTimer: Timer? = null

    var ref: String = ""

    var receivedResp: ReceivedResponse? = null

    private val _recHooks: MutableList<Hook> = mutableListOf()
    var recHooks: List<Hook> = _recHooks

    private var refEvent: String? = null

    fun resend(timeout: Long) {
        this.timeout = timeout
        cancelRefEvent()
        ref = ""
        refEvent = null
        receivedResp = null
        sent = false
        send()
    }

    fun send() {
        if (hasReceived("timeout")) {
            return
        }
        startTimeout()
        sent = true
        channel.socket.push(
            Message.StringMessage(
                topic = channel.topic,
                event = event,
                payload = payload as String,
                ref = ref,
            )
        )
    }

    fun receive(status: String, callback: (any: Any?) -> Unit): Push {
        if (hasReceived(status)) {
            callback(receivedResp?.response)
        }

        _recHooks.add(Hook(status, callback))
        return this
    }

    fun startTimeout() {
        if (timeoutTimer != null) {
            return
        }
        ref = makeRef()

        val eventName = channel.replyEventName(ref)
        refEvent = eventName

        channel.on(eventName) { payload, _ ->
            cancelRefEvent()
            cancelTimeout()
            receivedResp = payload as ReceivedResponse
            matchReceive(payload)
        }

        timeoutTimer = setTimeout({
            trigger("timeout") { /* Ignore response */ }
        }, timeout)
    }

    fun trigger(status: String, response: (any: Any?) -> Unit) {
        refEvent?.let { channel.trigger(it, ReceivedResponse(status, response), null) }
    }

    fun destroy() {
        cancelRefEvent()
        cancelTimeout()
    }

    private fun cancelRefEvent() {
        refEvent?.let { channel.off(it) }
    }

    private fun cancelTimeout() {
        timeoutTimer?.let { clearTimeout(it) }
        timeoutTimer = null
    }

    private fun matchReceive(recResponse: ReceivedResponse) {
        recHooks
            .filter { h -> h.status == recResponse.status }
            .forEach { h -> h.callback(recResponse.response) }
    }

    private fun hasReceived(status: String): Boolean {
        return receivedResp?.status == status
    }
}
