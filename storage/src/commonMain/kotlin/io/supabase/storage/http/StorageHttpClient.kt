package io.supabase.storage.http

import io.ktor.http.*

// TODO See if FetchOptions can be removed and headers directly accessed
data class FetchOptions(
    val headers: Headers = headersOf(),
    val noResolveJson: Boolean? = false
)

interface StorageHttpClient {

    val url: String

    val headers: Headers

    suspend fun get(path: String, options: FetchOptions?): String

    suspend fun post(path: String, options: FetchOptions?, body: Any?): String

    suspend fun put(path: String, options: FetchOptions?, body: Any): String

    suspend fun remove(path: String, options: FetchOptions?): String
}