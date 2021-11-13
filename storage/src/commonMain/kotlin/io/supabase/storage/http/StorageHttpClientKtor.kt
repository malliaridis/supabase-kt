package io.supabase.storage.http

import io.ktor.client.*
import io.ktor.client.request.*

/**
 * Default implementation of the [StorageHttpClient] used by the StorageDefaultHttpClient.
 *
 * Uses Ktor http client.
 */
class StorageHttpClientKtor(
    override val url: String,
    override val headers: Map<String, String>,
    private val httpClient: () -> HttpClient
) : StorageHttpClient {

    override suspend fun get(
        path: String,
        options: FetchOptions?
    ): String {
        return httpClient().get(url + path) {
            // TODO Handle options too
            // options
        }
    }

    override suspend fun post(
        path: String,
        options: FetchOptions?,
        body: Any?
    ): String {
        return httpClient().post(url + path) {
            // TODO Handle options too
            // options
            if (body != null) this.body = body
        }
    }

    override suspend fun put(
        path: String,
        options: FetchOptions?,
        body: Any
    ): String {
        return httpClient().put(url + path) {
            // TODO Handle options too
            // options
            this.body = body
        }
    }

    override suspend fun remove(
        path: String,
        options: FetchOptions?
    ): String {
        return httpClient().delete(url + path) {
            // TODO Handle options too
            // options
        }
    }
}