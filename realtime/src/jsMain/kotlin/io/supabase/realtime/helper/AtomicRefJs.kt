package io.supabase.realtime.helper

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate

private val current = atomic<Int>(0)

actual fun makeRef(): String = current.getAndUpdate { it + 1 }.toString()
