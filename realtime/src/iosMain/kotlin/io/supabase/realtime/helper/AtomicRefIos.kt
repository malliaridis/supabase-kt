package io.supabase.realtime.helper

import kotlin.native.concurrent.AtomicInt

private val current = AtomicInt(0)

actual fun makeRef(): String = current.addAndGet(1).toString()
