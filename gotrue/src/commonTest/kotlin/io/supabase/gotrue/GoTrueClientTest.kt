package io.supabase.gotrue

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import io.supabase.gotrue.domain.AppMetadata
import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.http.bodies.SignInEmailBody
import io.supabase.gotrue.http.errors.ServerError
import io.supabase.gotrue.http.results.SessionResult
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

internal class GoTrueClientTest {

    /**
     * Dummy API key
     */
    private val apiKey = "fakeApiKey"

    /**
     * Dummy token
     */
    private val token = "Bearer weird_token"

    /**
     * Dummy email
     */
    private val email = "my@email.com"

    /**
     * Dummy password
     */
    private val password = "password"

    private fun getMockClient(): HttpClient {
        return HttpClient(MockEngine) {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
            engine {
                addHandler { request ->
                    assertTrue(request.headers.contains("apikey"), "API key not included")
                    assertEquals(apiKey, request.headers["apikey"], "API key not the expected API key")
                    when (request.url.encodedPath) {
                        "/auth/v1/token" -> {
                            assertEquals(
                                ContentType.Application.Json,
                                request.body.contentType,
                                "Content type should be application/json"
                            )

                            val body =
                                Json.decodeFromString<SignInEmailBody>(request.body.toByteArray().decodeToString())
                            if (email == body.email && password == body.password) {
                                respond(
                                    content = Json.encodeToString(generateSession()),
                                    status = HttpStatusCode.OK,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            } else {
                                respond(
                                    content = Json.encodeToString(generateError()),
                                    status = HttpStatusCode.Unauthorized,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            }
                        }
                        "/auth/v1/user" -> {
                            val authHeader = request.headers["Authorization"]
                            if (authHeader != null) {
                                respond(
                                    content = Json.encodeToString(generateUser()),
                                    status = HttpStatusCode.OK,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            } else {
                                respond(
                                    content = Json.encodeToString(generateError()),
                                    status = HttpStatusCode.BadRequest,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            }
                        }
                        "/auth/v1/logout" -> {
                            val authHeader = request.headers["Authorization"]
                            if (authHeader != null) {
                                respond(
                                    content = "",
                                    status = HttpStatusCode.OK,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            } else {
                                respond(
                                    content = Json.encodeToString(generateError()),
                                    status = HttpStatusCode.Unauthorized,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            }
                        }
                        else -> fail("URL encoded should be one of the expected cases, but was ${request.url.encodedPath}")
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    fun signUp() {
    }

    @Test
    fun signIn_should_succeed_with_correct_credentials() = runTest {
        val client = GoTrueClient(
            url = "http://localhost:9999/auth/v1",
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        assertTrue(client.signIn(email = email, password = password) is SessionResult.Success)
    }

    @Test
    fun signIn_should_fail_with_invalid_credentials() = runTest {
        val client = GoTrueClient(
            url = "http://localhost:9999/auth/v1",
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
            url = "http://localhost:9999/auth/v1",
            headers = getUnauthenticatedHeaders(),
            httpClient = { getMockClient() }
        )

        assertTrue(client.user() == null, "User should be null when not authenticated")
    }

    @Test
    fun user_should_not_be_null_when_authenticated() = runTest {
        val client = GoTrueClient(
            url = "http://localhost:9999/auth/v1",
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
            url = "http://localhost:9999/auth/v1",
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

    private fun generateUser() = UserInfo(
        id = "some-uuid",
        aud = "my-aud",
        role = "authenticated",
        email = "my@email.com",
        emailConfirmedAt = Clock.System.now().toString(),
        phone = "",
        confirmationSentAt = Clock.System.now().toString(),
        confirmedAt = Clock.System.now().toString(),
        lastSignInAt = Clock.System.now().toString(),
        appMetadata = AppMetadata(),
        userMetadata = emptyMap(),
        createdAt = Clock.System.now().toString(),
        identities = emptyList()
    )

    private fun generateSession() = Session(
        accessToken = "some-access-token",
        expiresIn = 3600,
        refreshToken = "some-refresh-token",
        scope = null,
        user = generateUser(),
        tokenType = "bearer"
    )

    private fun generateError(): ServerError {
        return ServerError("error", "error description")
    }

    private fun getUnauthenticatedHeaders() = headersOf("apikey", apiKey)

    private fun getInvalidHeaders() = headersOf()
}