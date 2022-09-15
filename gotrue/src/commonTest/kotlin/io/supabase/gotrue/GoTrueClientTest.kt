package io.supabase.gotrue

import io.supabase.gotrue.http.results.SessionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class GoTrueClientTest {

    @Test
    @Ignore
    fun signUp() {
    }

    @Test
    @Ignore
    fun signIn_should_succeed_with_correct_credentials() = runTest {
        val client = getClient()

        assertTrue(client.signIn(email = email, password = password) is SessionResult.Success)
    }

    @Test
    @Ignore
    fun signIn_should_fail_with_invalid_credentials() = runTest {
        val client = getClient()

        assertTrue(client.signIn(email = email, password = "invalid") is SessionResult.Failure)
    }

    @Test
    @Ignore
    fun verifyOTP() {
    }

    @Test
    @Ignore
    fun update() {
    }

    @Test
    @Ignore
    fun signOut_should_succeed_after_successful_sign_in() {
    }

    @Test
    @Ignore
    fun signOut_should_return_null_if_not_signed_in() = runTest {
        val client = getClient()

        val error = client.signOut()
        assertNull(error, "Error should be returned when signed out without sign in.")
    }

    @Test
    @Ignore
    fun user_should_be_null_when_not_authenticated() {
        val client = getClient()

        assertTrue(client.user() == null, "User should be null when not authenticated")
    }

    @Test
    @Ignore
    fun user_should_not_be_null_when_authenticated() = runTest {
        val client = getClient()

        val result = client.signIn(email = email, password = password)

        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")
        assertTrue(client.user() != null, "User should not be null after successfully signed in.")
    }

    @Test
    @Ignore
    fun user_should_be_null_after_sign_out() = runTest {
        val client = getClient()

        val result = client.signIn(email = email, password = password)
        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")

        val error = client.signOut()
        assertTrue(error == null, "Sign out should not throw an error.")
        assertTrue(client.user() == null, "User should be null after successfully signed out.")
    }

    @Test
    @Ignore
    fun session_should_be_null_when_not_authenticated() = runTest {
        val client = getClient()

        assertNull(client.session(), "Session should be null before authentication.")
        val result = client.signIn(email = email, password = password)
        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")

        val error = client.signOut()
        assertTrue(error == null, "Sign out should not throw an error.")
        assertNull(client.session(), "Session should be null after logout.")
    }

    @Test
    @Ignore
    fun session_should_not_be_null_when_authenticated() = runTest {
        val client = getClient()

        val result = client.signIn(email = email, password = password)
        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")
        assertNotNull(client.session(), "Session should not be null after successful authentication.")
    }

    @Test
    @Ignore
    fun session_should_equal_sign_in_response_data() = runTest {
        val client = getClient()

        val result = client.signIn(email = email, password = password)
        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")
        assertNotNull(client.session(), "Session should not be null after successful authentication.")

        assertEquals(result.data, client.session(), "Session from session() should equal response data.")
    }

    @Test
    @Ignore
    fun refreshSession_should_update_existing_session() = runTest {
        val client = getClient()

        val result = client.signIn(email = email, password = password)
        assertTrue(result is SessionResult.Success)

        val oldSession = result.data

        val result2 = client.refreshSession()
        assertTrue(result2 is SessionResult.Success, "Refresh session should succeed after sign in.")
        val newSession = result2.data
        assertNotEquals(oldSession, newSession, "Session should be different after refreshToken().")
        assertEquals(newSession, client.session(), "session() sould return new session.")
    }

    @Test
    @Ignore
    fun getSessionFromUrl() {
    }

    @Test
    @Ignore
    fun onAuthStateChange() {
    }

    @Test
    @Ignore
    fun getAutoRefreshToken() {
    }

}