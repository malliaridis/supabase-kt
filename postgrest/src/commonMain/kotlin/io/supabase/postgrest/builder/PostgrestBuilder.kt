package io.supabase.postgrest.builder

import io.ktor.http.*
import io.supabase.postgrest.http.PostgrestHttpClient
import io.supabase.postgrest.http.PostgrestHttpResponse
import kotlinx.serialization.Serializable

open class PostgrestBuilder<T : @Serializable Any> {

    val httpClient: PostgrestHttpClient
    private val url: Url

    private var schema: String? = null
    private var headers: MutableMap<String, String> = mutableMapOf()
    private var method: HttpMethod? = null
    private var body: Any? = null
    private var searchParams: MutableMap<String, String> = mutableMapOf()

    constructor(builder: PostgrestBuilder<T>) {
        this.headers = builder.headers
        this.method = builder.method
        this.httpClient = builder.httpClient
        this.url = builder.url
        this.body = builder.body
        this.schema = builder.schema
    }

    constructor(url: Url, httpClient: PostgrestHttpClient, headers: Map<String, String>, schema: String?) {
        this.url = url
        this.httpClient = httpClient
        this.schema = schema

        headers.forEach { (name, value) -> setHeader(name, value) }
    }

    protected fun setHeader(name: String, value: String) {
        this.headers[name] = value
    }

    protected fun setSearchParam(name: String, value: String) {
        this.searchParams[name] = value
    }

    protected fun setMethod(method: HttpMethod) {
        this.method = method
    }

    protected fun setBody(body: Any?) {
        this.body = body
    }

    fun getSearchParams(): Map<String, String> {
        return searchParams
    }

    fun getBody(): Any? {
        return this.body
    }

    fun getHeaders(): Map<String, String> {
        return this.headers
    }

    fun getMethod(): HttpMethod? {
        return this.method
    }

    suspend fun <R : @Serializable Any> execute(): PostgrestHttpResponse<R> {
        checkNotNull(method) { "Method cannot be null" }

        // https://postgrest.org/en/stable/api.html#switching-schemas
        if (schema != null) {
            // skip
            if (this.method in listOf(HttpMethod.Get, HttpMethod.Head)) {
                setHeader("Accept-Profile", this.schema!!)
            } else {
                setHeader(HttpHeaders.ContentType, this.schema!!)
            }
        }

        if (this.method != HttpMethod.Get && this.method != HttpMethod.Head) {
            setHeader(HttpHeaders.ContentType, ContentType.Application.Json.contentType)
        }

        val uriParams = searchParams.toList().formUrlEncode()

        val uriWithParams = Url("${this.url}?${uriParams}")

        return httpClient.execute(
            uri = uriWithParams,
            method = method!!,
            headers = headers,
            body = body
        )
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