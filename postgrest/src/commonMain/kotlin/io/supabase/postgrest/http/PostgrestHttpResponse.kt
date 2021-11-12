package io.supabase.postgrest.http

data class PostgrestHttpResponse(
    val status: Int,
    val statusText: String,
    val body: String?,
    val count: Long?,
    val error: PostgrestError?
)

data class PostgrestError(
    val message: String,
    val details: String,
    val hint: String,
    val code: String
)