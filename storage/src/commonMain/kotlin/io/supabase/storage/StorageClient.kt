package io.supabase.storage

import io.ktor.client.*
import io.ktor.http.*

open class StorageClient(
    private val url: String,
    private val headers: Headers,
    private val httpClient: () -> HttpClient = { HttpClient() }
) : StorageBucketApi(url, headers, httpClient) {

    /**
     * Perform file operation in a bucket.
     *
     * @param id The bucket id to operate on.
     */
    fun from(id: String): StorageFileApi {
        return StorageFileApi(id, url, headers, httpClient)
    }
}