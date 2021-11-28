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
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
 * Dummy password
 */
const val password = "password"

const val authUrl = "http://localhost:9999/auth/v1"

fun getMockClient(): HttpClient {
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

fun generateUser() = UserInfo(
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
    accessToken = "some-access-token",
    expiresIn = 3600,
    refreshToken = "some-refresh-token",
    scope = null,
    user = generateUser(),
    tokenType = "bearer"
)

fun generateError(): ServerError {
    return ServerError("error", "error description")
}

fun getUnauthenticatedHeaders() = headersOf("apikey", apiKey)

fun getInvalidHeaders() = headersOf()