package io.supabase.realtime

import io.supabase.realtime.helper.*

data class Binding(
    val event: String,
    val callback: (data: Any?, ref: String?) -> Any?
)

class RealtimeSubscription(
    val topic: String,
    val params: Any?,
    val socket: RealtimeClient
) {

    private var bindings: MutableList<Binding> = mutableListOf()
    private val timeout: Long = this.socket.options?.timeout ?: DEFAULT_TIMEOUT
    private var state = ChannelState.closed
    private var joinedOnce = false
    private val joinPush: Push = Push(this, ChannelEvent.join, params, timeout)
    private val rejoinTimer: Timer = Timer({ rejoinUntilConnected() }, { tries -> socket.reconnectAfterMs(tries) })
    private val pushBuffer: MutableList<Push> = mutableListOf()

    init {

        joinPush.receive("ok") {
            state = ChannelState.joined
            rejoinTimer.reset()
            pushBuffer.forEach { pushEvent: Push -> pushEvent.send() }
            pushBuffer.clear()
        }

        onClose { _, _ ->
            rejoinTimer.reset()
            socket.log("channel", "close $topic ${joinRef()}")
            state = ChannelState.closed
            socket.remove(this)
        }

        onError { error ->
            if (isLeaving() || isClosed()) return@onError

            socket.log("channel", "error $topic", error.message ?: error.toString())
            state = ChannelState.errored
            rejoinTimer.scheduleTimeout()
        }

        joinPush.receive("timeout") {
            if (!isJoining()) return@receive

            socket.log("channel", "timeout ${this.topic}", joinPush.timeout)
            state = ChannelState.errored
            rejoinTimer.scheduleTimeout()
        }

        on(ChannelEvent.reply.toString()) { payload, ref ->
            // TODO see if null or ref should be passed at the end
            // TODO See if force unwrap will work here
            trigger(replyEventName(ref!!), payload, null)
        }
    }

    fun rejoinUntilConnected() {
        rejoinTimer.scheduleTimeout()
        if (socket.isConnected()) {
            rejoin()
        }
    }

    fun subscribe(timeout: Long = this.timeout): Push {
        if (joinedOnce) {
            throw Error("tried to subscribe multiple times. 'subscribe' can only be called a single time per channel instance")
        } else {
            joinedOnce = true
            rejoin(timeout)
            return joinPush
        }
    }

    fun onClose(callback: (any: Any?, ref: String?) -> Unit) {
        on(ChannelEvent.close.toString(), callback)
    }

    fun onError(callback: (reason: Error) -> Unit) {
        on(ChannelEvent.error.toString()) { reason, _ -> callback(reason as Error) }
    }

    fun on(event: String, callback: (any: Any?, ref: String?) -> Unit) {
        bindings.add(Binding(event, callback))
    }

    fun off(event: String) {
        bindings = bindings.filter { bind -> bind.event != event }.toMutableList()
    }

    fun canPush(): Boolean {
        return socket.isConnected() && isJoined()
    }

    fun push(event: ChannelEvent, payload: MessagePayload, timeout: Long = this.timeout): Push {
        if (!this.joinedOnce) {
            throw Error("tried to push '$event' to '$topic' before joining. Use channel.subscribe() before pushing events")
        }
        val pushEvent = Push(this, event, payload, timeout)
        if (this.canPush()) {
            pushEvent.send()
        } else {
            pushEvent.startTimeout()
            this.pushBuffer.add(pushEvent)
        }

        return pushEvent
    }

    /**
     * Leaves the channel
     *
     * Unsubscribes from server events, and instructs channel to terminate on server.
     * Triggers onClose() hooks.
     *
     * To receive leave acknowledgements, use the a `receive` hook to bind to the server ack, ie:
     * channel.unsubscribe().receive("ok", () => alert("left!") )
     */
    fun unsubscribe(timeout: Long = this.timeout): Push {
        this.state = ChannelState.leaving
        val onClose = {
            socket.log("channel", "leave $topic")
            trigger("close", "leave", joinRef())
        }
        // Destroy joinPush to avoid connection timeouts during unsubscription phase
        this.joinPush.destroy()

        val leavePush = Push(this, ChannelEvent.leave, null, timeout)
        leavePush.receive("ok") { onClose() }.receive("timeout") { onClose() }
        leavePush.send()
        if (!canPush()) leavePush.trigger("ok") {}


        return leavePush
    }

    /**
     * Overridable message hook
     *
     * Receives all events for specialized message handling before dispatching to the channel callbacks.
     * Must return the payload, modified or unmodified.
     */
    fun onMessage(event: String, payload: Any?, ref: String?): Any? {
        return payload
    }

    fun isMember(topic: String): Boolean {
        return this.topic == topic
    }

    fun joinRef(): String {
        return this.joinPush.ref
    }

    fun sendJoin(timeout: Long) {
        state = ChannelState.joining
        joinPush.resend(timeout)
    }

    fun rejoin(timeout: Long = this.timeout) {
        if (isLeaving()) {
            return
        }
        sendJoin(timeout)
    }

    fun trigger(event: String, payload: Any?, ref: String?) {

        if (ref != null && ref != joinRef()) {
            when (event) {
                ChannelEvent.close.toString(),
                ChannelEvent.error.toString(),
                ChannelEvent.leave.toString(),
                ChannelEvent.join.toString() -> return
                else -> { /* continue */
                }
            }
        }

        val handledPayload = onMessage(event, payload, ref)

        if (payload != null && handledPayload == null) {
            throw Error("channel onMessage callbacks must return the payload, modified or unmodified")
        }

        bindings
            .filter { bind ->
                // Bind all events if the user specifies a wildcard.
                if (bind.event == "*") event == (payload as? MessagePayload)?.type
                else bind.event == event
            }
            .map { bind -> bind.callback(handledPayload, ref) }
    }

    fun replyEventName(ref: String): String {
        return "chan_reply_$ref"
    }

    fun isClosed(): Boolean {
        return this.state == ChannelState.closed
    }

    fun isErrored(): Boolean {
        return this.state == ChannelState.errored
    }

    fun isJoined(): Boolean {
        return this.state == ChannelState.joined
    }

    fun isJoining(): Boolean {
        return this.state == ChannelState.joining
    }

    fun isLeaving(): Boolean {
        return this.state == ChannelState.leaving
    }
}
