package io.supabase.realtime.helper

// TODO Add atomic<Int>(0) again once atomicfu dependency stdlib bug fixed
private var current = 0

actual fun makeRef(): String = current++.toString()
