package io.supabase.postgrest

import io.supabase.postgrest.domain.Todo
import io.supabase.postgrest.helper.getClient
import io.supabase.postgrest.helper.getSamplesTodos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PostgrestClientTest {

    @Test
    fun should_get_simple_objects() = runTest {
        val client = getClient()
        val todos = client.from<Todo>("todos").select().executeAndGetList<Todo>()
        assertTrue(todos.isNotEmpty(), "Todos should be fetched successfully.")
        assertEquals(getSamplesTodos(), todos, "Todos should be same content.")
    }

    @Test
    fun should_get_simple_object_by_id() = runTest {
        val client = getClient()
        val todoId = "my-id"
        val todo = client.from<Todo>("todos").select().eq("id", todoId).executeAndGetSingle<Todo>()
        assertEquals(todoId, todo.id, "Todo id should be the requested one.")

        val todoId2 = "my-id2"
        val todo2 = client.from<Todo>("todos").select().eq("id", todoId2).executeAndGetSingle<Todo>()
        assertEquals(todoId2, todo2.id, "Todo id should be the requested one.")
    }
}