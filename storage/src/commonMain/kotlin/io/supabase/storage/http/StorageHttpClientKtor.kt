package io.supabase.storage.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Default implementation of the [StorageHttpClient] used by the StorageDefaultHttpClient.
 *
 * Uses Ktor http client.
 */
class StorageHttpClientKtor(
    override val url: String,
    override val headers: Headers,
    private val httpClient: () -> HttpClient
) : StorageHttpClient {

    override suspend fun get(
        path: String,
        options: FetchOptions?
    ): String {
        return httpClient().get(url + path) {
            // TODO Handle options too
            // options
        }.body()
    }

    override suspend fun post(
        path: String,
        options: FetchOptions?,
        body: Any?
    ): String {
        return httpClient().post(url + path) {
            // TODO Handle options too
            // options
            if (body != null) setBody(body)
        }.body()
    }

    override suspend fun put(
        path: String,
        options: FetchOptions?,
        body: Any
    ): String {
        return httpClient().put(url + path) {
            // TODO Handle options too
            // options
            setBody(body)
        }.body()
    }

    override suspend fun remove(
        path: String,
        options: FetchOptions?
    ): String {
        return httpClient().delete(url + path) {
            // TODO Handle options too
            // options
        }.body()
    }
}