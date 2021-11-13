package io.supabase.storage

import io.supabase.storage.http.StorageHttpClient

open class StorageClient(
    private val storageHttpClient: StorageHttpClient
) : StorageBucketApi(storageHttpClient.headers, storageHttpClient) {

    /**
     * Perform file operation in a bucket.
     *
     * @param id The bucket id to operate on.
     */
    fun from(id: String): StorageFileApi {
        return StorageFileApi(id, storageHttpClient)
    }
}