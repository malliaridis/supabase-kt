package io.supabase

import io.supabase.domain.Todo
import io.supabase.gotrue.http.results.SessionResult
import io.supabase.helper.getClient
import io.supabase.helper.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SupabaseClientTest {

    @Test
    fun signIn_should_work() = runTest {
        val supabase = getClient()
        val result = supabase.auth.signIn(email = "my@email.com", password = "password")
        assertTrue(result is SessionResult.Success, "Authentication should succeed")
        assertEquals(result.data, supabase.auth.session(), "Sign in sessions should be equal.")
    }

    @Test
    fun rest_request_should_be_authenticated() = runTest {
        val supabase = getClient()
        supabase.auth.signIn(email = "my@email.com", password = "password")

        val todos: List<Todo> = supabase.from<Todo>("todos").select().executeAndGetList()
        assertTrue(todos.isNotEmpty(), "TODOs should not be empty.")

    }
}
