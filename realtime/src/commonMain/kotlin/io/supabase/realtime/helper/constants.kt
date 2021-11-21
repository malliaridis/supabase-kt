package io.supabase.realtime.helper

const val version = "0.0.0"

val DEFAULT_HEADERS = mapOf("X-Client-Info" to "realtime-js/${version}")

const val VSN: String = "1.0.0"

const val DEFAULT_TIMEOUT = 10000L

enum class SocketState {
    connecting,
    open,
    closing,
    closed
}

enum class ChannelState {
    closed,
    errored,
    joined,
    joining,
    leaving
}

enum class ChannelEvent(event: String) {
    close("phx_close"),
    error("phx_error"),
    join("phx_join"),
    reply("phx_reply"),
    leave("phx_leave"),
    heartbeat("heartbeat"),
    any("*");

    companion object {
        fun from(event: String): ChannelEvent {
            return if (event == "*") any
            else valueOf(event)
        }
    }
}

enum class TRANSPORTS {
    websocket
}
