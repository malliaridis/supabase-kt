package io.supabase.realtime.helper

import java.util.concurrent.atomic.AtomicInteger

private val current = AtomicInteger()

actual fun makeRef() = current.incrementAndGet().toString()
