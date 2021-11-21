package io.supabase.realtime

import io.ktor.http.*

class RealtimeDefaultClient(
    url: Url,
    options: RealtimeClientOptions?,
    val apiKey: String
) : RealtimeClient(
    endpoint = url.toString(),
    options = options
)