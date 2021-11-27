package io.supabase.postgrest.builder

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.supabase.postgrest.http.PostgrestHttpResponse
import kotlinx.serialization.Serializable

open class PostgrestBuilder<T : @Serializable Any>(
    private val url: String,
    headers: Headers,
    private val schema: String,
    private val httpClient: () -> HttpClient
) {

    var headers: Headers
        get() = headersBuilder.build()
        set(value) = headersBuilder.appendAll(value)

    private var headersBuilder = HeadersBuilder()

    lateinit var method: HttpMethod
        protected set

    var body: @Serializable Any? = null
        protected set

    var searchParams: MutableMap<String, String> = mutableMapOf()
        private set

    constructor(builder: PostgrestBuilder<T>) : this(
        builder.url,
        builder.headers,
        builder.schema,
        builder.httpClient
    ) {
        this.method = builder.method
        this.body = builder.body
    }

    init {
        headersBuilder.appendAll(headers)
    }

    protected fun setHeader(name: String, value: String) {
        this.headersBuilder.append(name, value)
    }

    protected fun setSearchParam(name: String, value: String) {
        this.searchParams[name] = value
    }

    suspend fun <R : @Serializable Any> execute(): PostgrestHttpResponse<R> {
        // https://postgrest.org/en/stable/api.html#switching-schemas

        if (method in listOf(HttpMethod.Get, HttpMethod.Head)) {
            headersBuilder.append("Accept-Profile", schema)
        } else {
            headersBuilder.append(HttpHeaders.ContentType, schema)
        }

        if (method != HttpMethod.Get && method != HttpMethod.Head) {
            headersBuilder.append(HttpHeaders.ContentType, ContentType.Application.Json.contentType)
        }

        val uriParams = searchParams.toList().formUrlEncode()

        return httpClient().request("$url?$uriParams") {
            method = this@PostgrestBuilder.method
            headers { appendAll(this@PostgrestBuilder.headers) }
            this@PostgrestBuilder.body?.let { body = it }
        }
    }

//    suspend inline fun <reified R : Any> executeAndGetSingle(): R {
//        val response = execute<R>()
//        return Json.decodeFromString(response.body!!)
//    }
//
//    suspend inline fun <reified R : Any> executeAndGetList(): List<R> {
//        val response = execute<R>()
//        return Json.decodeFromString(response.body!!)
//    }
}