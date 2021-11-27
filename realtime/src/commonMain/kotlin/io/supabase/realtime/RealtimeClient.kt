package io.supabase.realtime

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.collections.*
import io.supabase.realtime.helper.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * The options that can be passed for initializing the socket.
 *
// * @param transport The Websocket Transport, for example WebSocket.
 * @param httpClient The HTTP client on which the websocket is running.
 * @param timeout The default timeout in milliseconds to trigger push timeouts.
 * @param params The optional params to pass when connecting.
 * @param headers The optional headers to pass when connecting.
 * @param heartbeatIntervalMs The milliseconds' interval to send a heartbeat message.
 * @param logger The optional function for specialized logging, ie: logger: (kind, msg, data) => { console.log(`${kind}: ${msg}`, data) }
 * @param longPollerTimeout The maximum timeout of a long poll AJAX request. Defaults to 20s (double the server long poll timer).
 * @param reconnectAfterMs The optional function that returns the milliseconds reconnect interval. Defaults to stepped backoff off.
 */
data class RealtimeClientOptions(
//    val transport: WebSocketSession?,
    val httpClient: HttpClient,
    val timeout: Long?,
    val heartbeatIntervalMs: Long?,
    val longPollerTimeout: Long?,
    val logger: ((kind: String, msg: String, data: Any?) -> Unit)?,
    val reconnectAfterMs: ((tries: Int) -> Long)?,
    val headers: Headers?,
    val params: Map<String, String>?
)

@Serializable
sealed class Message(
    open val topic: String = "",
    open val event: ChannelEvent = ChannelEvent.any,
    open val payload: @Contextual Any?,
    open val ref: String? = null
) {
    data class StringMessage(
        override val topic: String = "",
        override val event: ChannelEvent = ChannelEvent.any,
        override val payload: String,
        override val ref: String? = null
    ) : Message(topic, event, payload, ref)

    data class UnitMessage(
        override val topic: String = "",
        override val event: ChannelEvent = ChannelEvent.any,
        override val payload: Unit?,
        override val ref: String? = null
    ) : Message(topic, event, payload, ref)

    data class JsonMessage(
        override val topic: String = "",
        override val event: ChannelEvent = ChannelEvent.any,
        override val payload: JsonElement,
        override val ref: String? = null
    ) : Message(topic, event, payload, ref)
}

@Serializable
data class MessagePayload(
    val status: String,
    val type: String
)

data class StateChangeCallbacks(
    val open: MutableList<() -> Unit> = mutableListOf(),
    val close: MutableList<(any: Any?) -> Unit> = mutableListOf(),
    val error: MutableList<(error: Exception) -> Unit> = mutableListOf(),
    val message: MutableList<(msg: Any) -> Unit> = mutableListOf()
)

/**
 * Initializes the Socket
 *
 * @param url The string WebSocket endpoint, ie, "ws://example.com/socket", "wss://example.com", "/socket" (inherited host & protocol)
 */
open class RealtimeClient(
    private val url: String,
    internal val options: RealtimeClientOptions?
) {

    private val headers: Headers = options?.headers?.let { TODO("Merge headers") } ?: DEFAULT_HEADERS
    private val params: Map<String, String> = options?.params ?: emptyMap()

    private val heartbeatIntervalMs: Long = options?.heartbeatIntervalMs ?: 30000

    private val httpClient: HttpClient = options?.httpClient ?: HttpClient {
        install(WebSockets) {
            pingInterval = options?.heartbeatIntervalMs ?: 30000
        }
    }
    private val longPollerTimeout: Long = options?.longPollerTimeout ?: 20000

    private var websocketJob: Job? = null
    var channels: MutableList<RealtimeSubscription> = mutableListOf()
    private var heartbeatTimer: Timer? = null
    private var pendingHeartbeatRef: String? = null
    private val reconnectTimer: Timer = Timer(
        callback = suspend {
            disconnect(CloseReason.Codes.TRY_AGAIN_LATER, "")
            connect()
        },
        timerCalc = { tries -> reconnectAfterMs(tries) }
    )

    private var conn: WebSocketSession? = null
    private var socketState: SocketState = SocketState.closed

    private var sendBuffer: ConcurrentList<suspend () -> Unit?> = ConcurrentList()

    private val stateChangeCallbacks = StateChangeCallbacks()

    internal fun reconnectAfterMs(tries: Int): Long {
        return options?.reconnectAfterMs?.invoke(tries)
            ?: listOf<Long>(1000, 2000, 5000, 10000).getOrNull(tries - 1)
            ?: 10000L
    }

    /**
     * Connects the socket.
     */
    fun connect() {
        if (websocketJob != null) return
        // if (conn != null) return

        socketState = SocketState.connecting
        websocketJob = CoroutineScope(Dispatchers.Default).launch {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = url,
                port = 8080,
                path = "/websocket?vsn=1.0.0",
                request = {
                    headers { appendAll(this@RealtimeClient.headers) }
                    timeout {
                        // connectTimeoutMillis = longPollerTimeout
                        socketTimeoutMillis = longPollerTimeout
                    }
                }
            ) {
                conn = this
                onConnOpen()
                socketState = SocketState.open

                val messageOutputRoutine = launch { outputMessages() }
                val userInputRoutine = launch { inputMessages() }

                userInputRoutine.join()
                socketState = SocketState.closing
                messageOutputRoutine.cancelAndJoin()
            }

            socketState = SocketState.closed
            onConnClose(null)
            websocketJob = null
        }
    }

    /**
     * Disconnects the socket.
     *
     * @param code A numeric status code to send on disconnect.
     * @param reason A custom reason for the disconnect.
     */
    suspend fun disconnect(code: CloseReason.Codes?, reason: String?): Boolean {
        with(conn) {
            if (this != null) {
                onClose { }
                if (code != null) close(CloseReason(code, reason ?: ""))
                else close()
            }

            conn = null

            // remove open handles
            heartbeatTimer?.let { clearInterval(it) }
            reconnectTimer.reset()
        }
        return true
    }

    /**
     * Logs the message. Override `this.logger` for specialized logging.
     */
    internal fun log(kind: String, msg: String, data: Any? = null) {
        options?.logger?.invoke(kind, msg, data)
    }

    /**
     * Registers a callback for connection state change event.
     * @param callback A function to be called when the event occurs.
     *
     * @example
     *    socket.onOpen(() => console.log("Socket opened."))
     */
    fun onOpen(callback: () -> Unit) {
        this.stateChangeCallbacks.open.add(callback)
    }

    /**
     * Registers a callbacks for connection state change events.
     * @param callback A function to be called when the event occurs.
     *
     * @example
     *    socket.onOpen(() => console.log("Socket closed."))
     */
    private fun onClose(callback: (any: Any?) -> Unit) {
        this.stateChangeCallbacks.close.add(callback)
    }

    /**
     * Registers a callback for connection state change events.
     * @param callback A function to be called when the event occurs.
     *
     * @example
     *    socket.onOpen((error) => console.log("An error occurred"))
     */
    fun onError(callback: (error: Exception) -> Unit) {
        this.stateChangeCallbacks.error.add(callback)
    }

    /**
     * Calls a function any time a message is received.
     * @param callback A function to be called when the event occurs.
     *
     * @example
     *    socket.onMessage((message) => console.log(message))
     */
    fun onMessage(callback: (msg: Any) -> Unit) {
        this.stateChangeCallbacks.message.add(callback)
    }

    /**
     * Returns the current state of the socket.
     */
    fun connectionState(): SocketState {
        return socketState
    }

    /**
     * Returns `true` is the connection is open.
     */
    fun isConnected(): Boolean {
        return socketState == SocketState.open
    }

    /**
     * Removes a subscription from the socket.
     *
     * @param channel An open subscription.
     */
    fun remove(channel: RealtimeSubscription) {
        channels = channels.filter { c: RealtimeSubscription -> c.joinRef() != channel.joinRef() }.toMutableList()
    }

    fun channel(topic: String, params: Any?): RealtimeSubscription {
        val chan = RealtimeSubscription(topic, params, this)
        channels.add(chan)
        return chan
    }

    fun push(data: Message) {
        val callback = suspend {
            conn?.send(Json.encodeToString(data))
        }
        log("push", "${data.topic} ${data.event} (${data.ref})", data.payload)

        // TODO Check if change is valid
        // if (isConnected()) callback()
        // else sendBuffer.add(callback)
        sendBuffer.add(callback)
    }

    private fun onConnMessage(rawMessage: String) {
        val msg = Json.decodeFromString<Message>(rawMessage)

        if (msg.ref == pendingHeartbeatRef) pendingHeartbeatRef = null
        else if (msg.event.toString() == (msg.payload as? MessagePayload)?.type) resetHeartbeat()

        log(
            "receive",
            "${(msg.payload as? MessagePayload)?.status ?: ""} ${msg.topic} ${msg.event} ${if (msg.ref != null) "(${msg.ref})" else ""}",
            msg.payload
        )

        channels
            .filter { channel: RealtimeSubscription -> channel.isMember(msg.topic) }
            .forEach { channel: RealtimeSubscription -> channel.trigger(msg.event.toString(), msg.payload, msg.ref) }
        stateChangeCallbacks.message.forEach { callback -> callback.invoke(msg) }

    }

    /**
     * Return the next message ref, accounting for overflows
     */
    private fun getNextRef(): String {
        return makeRef()
    }

    private suspend fun onConnOpen() {
        log("transport", "connected to $url$")
        flushSendBuffer()
        reconnectTimer.reset()
        resetHeartbeat()
        stateChangeCallbacks.open.forEach { callback -> callback.invoke() }
    }

    private fun onConnClose(event: Any?) {
        log("transport", "close", event)
        triggerChanError()
        heartbeatTimer?.let { clearInterval(it) }
        reconnectTimer.scheduleTimeout()
        stateChangeCallbacks.close.forEach { callback -> callback.invoke(event) }
    }

    private fun onConnError(error: Exception) {
        log("transport", error.message ?: error.toString())
        triggerChanError()
        stateChangeCallbacks.error.forEach { callback -> callback.invoke(error) }
    }

    private fun triggerChanError() {
        channels.forEach { channel: RealtimeSubscription -> channel.trigger(ChannelEvent.error.toString(), null, null) }
    }

    private suspend fun flushSendBuffer() {
        if (isConnected()) {
            sendBuffer.forEach { callback -> callback.invoke() }
            sendBuffer.clear()
        }
    }

    private fun resetHeartbeat() {
        pendingHeartbeatRef = null
        heartbeatTimer?.let { clearInterval(it) }
        heartbeatTimer = setInterval(suspend { sendHeartbeat() }, heartbeatIntervalMs)
    }

    private suspend fun sendHeartbeat() {
        if (!isConnected()) {
            return
        }
        if (pendingHeartbeatRef != null) {
            pendingHeartbeatRef = null
            log("transport", "heartbeat timeout. Attempting to re-establish connection")

            conn?.close(CloseReason(CloseReason.Codes.NORMAL, "hearbeat timeout"))
            return
        }
        pendingHeartbeatRef = getNextRef()
        push(Message.UnitMessage("phoenix", ChannelEvent.heartbeat, null, pendingHeartbeatRef))
    }

    private suspend fun DefaultClientWebSocketSession.outputMessages() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                onConnMessage(message.readText())
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.message)
            onConnError(e)
        }
    }

    private suspend fun DefaultClientWebSocketSession.inputMessages() {
        while (true) {
            try {
                flushSendBuffer()
            } catch (e: Exception) {
                println("Error while sending: " + e.message)
                onConnError(e)
                return
            }
        }
    }
}


