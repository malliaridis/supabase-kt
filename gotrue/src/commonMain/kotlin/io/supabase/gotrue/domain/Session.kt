package io.supabase.gotrue.domain

import io.supabase.gotrue.helper.expiresAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property accessToken JWT representing the user.
 * @property expiresIn The number of seconds until the token expires (since it was issued). Returned when a login is
 * confirmed.
 * @property expiresAt A timestamp of when the token will expire. Returned when a login is confirmed.
 */
@Serializable
data class Session(

    @SerialName("provider_token") val providerToken: String? = null,

    @SerialName("access_token") val accessToken: String,

    @SerialName("expires_in") val expiresIn: Long? = null,

    @SerialName("expires_at") val expiresAt: Long? = expiresIn?.let { expiresAt(it) },

    @SerialName("refresh_token") val refreshToken: String? = null,

    @SerialName("token_type") val tokenType: String = "",

    val user: User? = null
)