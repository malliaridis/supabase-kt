package io.supabase.postgrest.builder

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.supabase.postgrest.http.PostgrestHttpResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

open class PostgrestBuilder<T : @Contextual Any>(
    private val url: String,
    private var headers: Headers,
    private val schema: String,
    private val httpClient: () -> HttpClient
) {

    lateinit var method: HttpMethod
        protected set

    var body: @Serializable Any? = null
        protected set

    // TODO Make searchParams String: Serializable
    var searchParams: MutableMap<String, String> = mutableMapOf()
        private set

    constructor(builder: PostgrestBuilder<T>) : this(
        builder.url,
        builder.headers,
        builder.schema,
        builder.httpClient
    ) {
        this.method = builder.method
        this.searchParams = builder.searchParams
        this.body = builder.body
    }

    protected fun setHeader(name: String, value: String) {
        headers = Headers.build {
            appendAll(headers)
            append(name, value)
        }
    }

    protected fun setSearchParam(name: String, value: String) {
        this.searchParams[name] = value
    }

    suspend fun execute(): PostgrestHttpResponse<JsonElement> {
        // https://postgrest.org/en/stable/api.html#switching-schemas

        if (method in listOf(HttpMethod.Get, HttpMethod.Head)) {
            setHeader("Accept-Profile", schema)
        } else {
            setHeader(HttpHeaders.ContentType, schema)
        }

        if (method != HttpMethod.Get && method != HttpMethod.Head) {
            setHeader(HttpHeaders.ContentType, ContentType.Application.Json.contentType)
        }

        val uriParams = searchParams.toList().formUrlEncode()

        val response = httpClient().request("$url?$uriParams") {
            method = this@PostgrestBuilder.method
            headers { appendAll(this@PostgrestBuilder.headers) }
            this@PostgrestBuilder.body?.let { setBody(it) }
        }

        var count: Long? = null
        if (response.status == HttpStatusCode.OK) {
            val countHeader = this.headers["Prefer"]?.matches(Regex("count=(exact|planned|estimated)"))
            val contentRange = response.headers["content-range"]?.split('/')
            if (countHeader == true && contentRange != null && contentRange.size > 1) {
                count = contentRange[1].toLongOrNull()
            }
        }
        return if (response.status.isSuccess()) return PostgrestHttpResponse.Success(
            status = response.status.value,
            body = json.decodeFromString(response.bodyAsText()),
            statusText = response.status.description,
            count = count
        ) else PostgrestHttpResponse.Failure(
            status = response.status.value,
            body = null,
            statusText = response.status.description,
            count = count,
            error = json.decodeFromString(response.bodyAsText())
        )
    }

    val json = Json { ignoreUnknownKeys = true }

    suspend inline fun <reified R : @Serializable Any> execute(json: Json = this.json): PostgrestHttpResponse<R> {
        return when (val result = execute()) {
            is PostgrestHttpResponse.Failure -> {
                PostgrestHttpResponse.Failure(
                    status = result.status,
                    statusText = result.statusText,
                    body = null,
                    count = result.count,
                    error = result.error,
                )
            }

            is PostgrestHttpResponse.Success -> {
                PostgrestHttpResponse.Success(
                    status = result.status,
                    statusText = result.statusText,
                    body = json.decodeFromJsonElement<R>(result.body),
                    count = result.count,
                )
            }
        }
    }
}