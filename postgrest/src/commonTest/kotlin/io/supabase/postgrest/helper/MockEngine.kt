package io.supabase.postgrest.helper

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.supabase.postgrest.PostgrestClient
import io.supabase.postgrest.domain.ServerError
import io.supabase.postgrest.domain.Todo
import io.supabase.postgrest.http.PostgrestHttpResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.fail

const val restUrl = "http://localhost:9999/rest/v1"

fun getMockClient(): HttpClient {
    return HttpClient(MockEngine) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/rest/v1/todos" -> {
                        respond(
                            content = Json.encodeToString(getSamplesTodos()),
                            status = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.ContentType to listOf("application/json"),
                                HttpHeaders.ContentRange to listOf("0-1/*") // starting at 0 ends (incl.) 1, * because all elements fetched
                            )
                        )
                    }
                    else -> fail("URL encoded should be one of the expected cases, but was ${request.url.encodedPath}")
                }
            }
        }
    }
}

fun generateError(): ServerError {
    return ServerError("error", "error description")
}

fun getClient() = PostgrestClient(
    url = restUrl,
    headers = headersOf(),
    httpClient = { getMockClient() }
)

fun getSampleResponse() = PostgrestHttpResponse(
    status = 200,
    statusText = "OK",
    body = getSamplesTodos(),
    count = 2, error = null
)

fun getSamplesTodos() = listOf(Todo("my-id", "my text"), Todo("my-id-2", "my text 2"))

fun getInvalidHeaders() = headersOf()