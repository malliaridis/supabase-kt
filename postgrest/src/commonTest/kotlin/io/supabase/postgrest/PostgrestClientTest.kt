package io.supabase.postgrest

import io.supabase.postgrest.domain.Todo
import io.supabase.postgrest.helper.getClient
import io.supabase.postgrest.helper.getSamplesTodos
import io.supabase.postgrest.helper.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PostgrestClientTest {

    @Test
    fun should_get_simple_objects() = runTest {
        val client = getClient()
        val todos = client.from<Todo>("todos").select().executeAndGetList<Todo>()
        assertTrue(todos.isNotEmpty(), "Todos should be fetched successfully.")
        assertEquals(getSamplesTodos(), todos, "Todos should be same content.")
    }
}