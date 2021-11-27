package io.supabase.storage

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.supabase.storage.types.Bucket
import kotlinx.serialization.Serializable

@Serializable
data class BucketCreateOptions(
    val id: String,
    val public: Boolean
)

open class StorageBucketApi(
    private val url: String,
    private val headers: Headers,
    private val httpClient: () -> HttpClient
) {

    /**
     * Retrieves the details of all Storage buckets within an existing product.
     */
    suspend fun listBuckets(): List<Bucket> {
        return httpClient().get("$url/bucket") {
            headers {
                appendAll(this@StorageBucketApi.headers)
            }
        }
    }

    /**
     * Retrieves the details of an existing Storage bucket.
     *
     * @param id The unique identifier of the bucket you would like to retrieve.
     */
    suspend fun getBucket(id: String): Bucket {
        return httpClient().get("$url/bucket/${id}") {
            headers {
                appendAll(this@StorageBucketApi.headers)
            }
        }
    }

    /**
     * Creates a new Storage bucket
     *
     * @param id A unique identifier for the bucket you are creating.
     * @returns newly created bucket id
     */
    suspend fun createBucket(id: String, public: Boolean = false): String {
        // TODO Check if this response could be deserialized
        return httpClient().post("$url/bucket") {
            headers {
                appendAll(this@StorageBucketApi.headers)
            }
            body = BucketCreateOptions(id, public)
        }
    }

    /**
     * Updates a new Storage bucket
     *
     * @param id A unique identifier for the bucket you are creating.
     */
    suspend fun updateBucket(id: String, public: Boolean): String {
        // TODO Check if this response could be deserialized
        return httpClient().put("$url/bucket/${id}") {
            headers {
                appendAll(this@StorageBucketApi.headers)
            }
            body = BucketCreateOptions(id, public)
        }
    }

    /**
     * Removes all objects inside a single bucket.
     *
     * @param id The unique identifier of the bucket you would like to empty.
     */
    suspend fun emptyBucket(id: String): String {
        return httpClient().post("$url/bucket/${id}/empty") {
            headers {
                appendAll(this@StorageBucketApi.headers)
            }
        }
    }

    /**
     * Deletes an existing bucket. A bucket can't be deleted with existing objects inside it.
     * You must first `empty()` the bucket.
     *
     * @param id The unique identifier of the bucket you would like to delete.
     */
    suspend fun deleteBucket(id: String): String {
        return httpClient().delete("$url/bucket/$id") {
            headers {
                appendAll(this@StorageBucketApi.headers)
            }
        }
    }
}
