package io.supabase.gotrue

import kotlinx.coroutines.runBlocking

actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }