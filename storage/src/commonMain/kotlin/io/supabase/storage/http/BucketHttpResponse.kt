package io.supabase.storage.http

import kotlinx.serialization.Serializable

@Serializable
data class BucketHttpResponse<T : @Serializable Any>(
    val data: T?,
    val error: BucketError?
)

@Serializable
data class BucketError(
    val name: String,
    val message: String,
    val stack: String?
)