package io.supabase.postgrest.http

import kotlinx.serialization.Serializable

@Serializable
sealed class PostgrestHttpResponse<T> {

    @Serializable
    data class Success<T>(
        val status: Int,
        val statusText: String,
        val body: T,
        val count: Long?
    ) : PostgrestHttpResponse<T>()

    @Serializable
    data class Failure<T>(
        val status: Int,
        val statusText: String,
        val body: T?,
        val count: Long?,
        val error: PostgrestError
    ) : PostgrestHttpResponse<T>()
}
