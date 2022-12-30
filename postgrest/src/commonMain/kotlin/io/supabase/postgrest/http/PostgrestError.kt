package io.supabase.postgrest.http

import kotlinx.serialization.Serializable

@Serializable
data class PostgrestError(
    val message: String,
    val details: String?,
    val hint: String?,
    val code: String
)