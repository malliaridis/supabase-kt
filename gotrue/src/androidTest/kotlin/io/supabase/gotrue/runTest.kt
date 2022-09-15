package io.supabase.gotrue

import kotlinx.coroutines.runBlocking
import kotlin.test.*

actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }
