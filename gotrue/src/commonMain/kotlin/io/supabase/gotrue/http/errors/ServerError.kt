package io.supabase.gotrue.http.errors

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerError(
    val error: String,
    @SerialName("error_description") val errorDescription: String
)
