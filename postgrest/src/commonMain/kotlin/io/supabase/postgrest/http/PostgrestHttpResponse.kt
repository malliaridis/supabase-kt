package io.supabase.postgrest.http

import kotlinx.serialization.Serializable

@Serializable
data class PostgrestHttpResponse<T : @Serializable Any>(
    val status: Int,
    val statusText: String,
    val body: T?,
    val count: Long?,
    val error: PostgrestError?
)

@Serializable
data class PostgrestError(
    val message: String,
    val details: String,
    val hint: String,
    val code: String
)