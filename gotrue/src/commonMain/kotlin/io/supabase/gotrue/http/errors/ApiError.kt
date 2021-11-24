package io.supabase.gotrue.http.errors

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    override val message: String,
    val status: Int
) : Throwable(message)