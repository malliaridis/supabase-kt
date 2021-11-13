package io.supabase.storage

import io.supabase.storage.http.FetchOptions
import io.supabase.storage.http.StorageHttpClient
import io.supabase.storage.json.deserialize
import io.supabase.storage.types.Bucket
import kotlinx.serialization.Serializable

@Serializable
data class BucketCreateOptions(
    val id: String,
    val public: Boolean
)

open class StorageBucketApi(
    private val headers: Map<String, String>,
    private val httpClient: StorageHttpClient
) {

    /**
     * Retrieves the details of all Storage buckets within an existing product.
     */
    suspend fun listBuckets(): List<Bucket> {
        val result = httpClient.get("/bucket", FetchOptions(headers, false))
        return deserialize(result)
    }

    /**
     * Retrieves the details of an existing Storage bucket.
     *
     * @param id The unique identifier of the bucket you would like to retrieve.
     */
    suspend fun getBucket(id: String): Bucket {
        val result = httpClient.get("/bucket/${id}", FetchOptions(headers, false))
        return deserialize(result)
    }

    /**
     * Creates a new Storage bucket
     *
     * @param id A unique identifier for the bucket you are creating.
     * @returns newly created bucket id
     */
    suspend fun createBucket(id: String, public: Boolean = false): String {
        return httpClient.post(
            path = "/bucket",
            options = FetchOptions(headers, true),
            body = BucketCreateOptions(id, public)
        )
    }

    /**
     * Updates a new Storage bucket
     *
     * @param id A unique identifier for the bucket you are creating.
     */
    suspend fun updateBucket(id: String, public: Boolean): String {
        return httpClient.put(
            path = "/bucket/${id}",
            options = FetchOptions(headers, true),
            body = BucketCreateOptions(id, public)
        )
    }

    /**
     * Removes all objects inside a single bucket.
     *
     * @param id The unique identifier of the bucket you would like to empty.
     */
    suspend fun emptyBucket(id: String): String {
        return httpClient.post(
            path = "/bucket/${id}/empty",
            options = FetchOptions(headers, true),
            body = null
        )
    }

    /**
     * Deletes an existing bucket. A bucket can't be deleted with existing objects inside it.
     * You must first `empty()` the bucket.
     *
     * @param id The unique identifier of the bucket you would like to delete.
     */
    suspend fun deleteBucket(id: String): String {
        return httpClient.remove(
            path = "/bucket/${id}",
            options = FetchOptions(headers, true)
        )
    }
}
