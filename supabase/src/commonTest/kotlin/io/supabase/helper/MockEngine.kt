package io.supabase.helper

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.supabase.SupabaseClient
import io.supabase.gotrue.domain.AppMetadata
import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.bodies.RefreshAccessTokenBody
import io.supabase.gotrue.http.bodies.SignInEmailBody
import io.supabase.gotrue.http.errors.ServerError
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Dummy API key
 */
const val apiKey = "fakeApiKey"

/**
 * Dummy email
 */
const val email = "my@email.com"

/**
 * Dummy access token
 */
private var accessToken = ""

/**
 * Dummy refresh token
 */
private var refreshToken = ""

/**
 * Dummy password
 */
const val password = "password"

const val supabaseUrl = "http://localhost:9999"

const val authUrl = "$supabaseUrl/auth/v1"

fun getMockClient(): HttpClient {
    return HttpClient(MockEngine) {
        install(ContentNegotiation) { json() }
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

                        if (request.url.parameters.contains("grant_type", "refresh_token")) {
                            handleRefresh(request)
                        } else handleSignIn(request)
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

private suspend fun MockRequestHandleScope.handleSignIn(request: HttpRequestData): HttpResponseData {
    val body = Json.decodeFromString<SignInEmailBody>(request.body.toByteArray().decodeToString())
    return if (email == body.email && password == body.password) {
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

private suspend fun MockRequestHandleScope.handleRefresh(request: HttpRequestData): HttpResponseData {
    return try {
        val body = Json.decodeFromString<RefreshAccessTokenBody>(request.body.toByteArray().decodeToString())
        if (refreshToken == body.refreshToken) {
            respond(
                content = Json.encodeToString(generateSession()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        } else {
            respond(
                content = Json.encodeToString(ServerError("invalid_grant", "Invalid Refresh Token")),
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    } catch (_: Exception) {
        respond(
            content = Json.encodeToString(ServerError("invalid_request", "refresh_token required")),
            status = HttpStatusCode.BadRequest,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
}

fun generateUser() = User(
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

fun generateSession() = Session(
    accessToken = generateAccessToken(),
    expiresIn = 3600,
    refreshToken = generateRefreshToken(),
    user = generateUser(),
    tokenType = "bearer"
)

fun generateAccessToken(): String {
    accessToken = "access-token-${Random.nextInt()}"
    return accessToken
}

fun generateRefreshToken(): String {
    refreshToken = "refresh-token-${Random.nextInt()}"
    return refreshToken
}

fun generateError(): ServerError {
    return ServerError("error", "error description")
}

fun getUnauthenticatedHeaders() = headersOf("apikey", apiKey)

fun getClient() = SupabaseClient(
    supabaseUrl = supabaseUrl,
    supabaseKey = apiKey,
    headers = getUnauthenticatedHeaders(),
    httpClient = { getMockClient() }
)

fun getInvalidHeaders() = headersOf()