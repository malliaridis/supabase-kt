package io.supabase.gotrue

import io.supabase.gotrue.http.results.SessionResult
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GoTrueClientTest {

    @Test
    @Ignore
    fun signUp() {
    }

    @Test
    fun signIn_should_succeed_with_correct_credentials() = runTest {
        val client = GoTrueClient(
            url = authUrl,
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        assertTrue(client.signIn(email = email, password = password) is SessionResult.Success)
    }

    @Test
    fun signIn_should_fail_with_invalid_credentials() = runTest {
        val client = GoTrueClient(
            url = authUrl,
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        assertTrue(client.signIn(email = email, password = "invalid") is SessionResult.Failure)
    }

    @Test
    @Ignore
    fun verifyOTP() {
    }

    @Test
    @Ignore
    fun refreshSession() {
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
    fun signOut_should_fail_when_not_signed_in() {
    }

    @Test
    fun user_should_be_null_when_not_authenticated() {
        val client = GoTrueClient(
            url = authUrl,
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        assertTrue(client.user() == null, "User should be null when not authenticated")
    }

    @Test
    fun user_should_not_be_null_when_authenticated() = runTest {
        val client = GoTrueClient(
            url = authUrl,
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        val result = client.signIn(email = email, password = password)

        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")
        assertTrue(client.user() != null, "User should not be null after successfully signed in.")
    }

    @Test
    fun user_should_be_null_after_sign_out() = runTest {
        val client = GoTrueClient(
            url = authUrl,
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        val result = client.signIn(email = email, password = password)
        assertTrue(result is SessionResult.Success, "Sign in should succeed with correct credentials.")

        val error = client.signOut()
        assertTrue(error == null, "Sign out should not throw an error.")
        assertTrue(client.user() == null, "User should be null after successfully signed out.")
    }

    @Test
    @Ignore
    fun session_should_be_null_when_not_authenticated() {
    }

    @Test
    @Ignore
    fun session_should_not_be_null_when_authenticated() {
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