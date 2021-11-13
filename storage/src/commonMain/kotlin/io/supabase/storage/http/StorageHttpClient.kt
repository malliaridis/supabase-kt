package io.supabase.storage.http

// TODO See if FetchOptions can be removed and headers directly accessed
data class FetchOptions(
    val headers: Map<String, String>,
    val noResolveJson: Boolean?
)

interface StorageHttpClient {

    val url: String

    val headers: Map<String, String>

    suspend fun get(path: String, options: FetchOptions?): String

    suspend fun post(path: String, options: FetchOptions?, body: Any?): String

    suspend fun put(path: String, options: FetchOptions?, body: Any): String

    suspend fun remove(path: String, options: FetchOptions?): String
}