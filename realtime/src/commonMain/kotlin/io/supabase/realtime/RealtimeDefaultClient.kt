package io.supabase.realtime

class RealtimeDefaultClient(
    url: String,
    options: RealtimeClientOptions?,
    val apiKey: String
) : RealtimeClient(
    url = url,
    options = options
)