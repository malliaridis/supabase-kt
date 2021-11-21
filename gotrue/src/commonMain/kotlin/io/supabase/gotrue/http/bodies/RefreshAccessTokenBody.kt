package io.supabase.gotrue.http.bodies

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshAccessTokenBody(
    @SerialName("refresh_token")
    val refreshToken: String
)