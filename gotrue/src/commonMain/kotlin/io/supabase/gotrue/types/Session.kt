package io.supabase.gotrue.types

import io.supabase.gotrue.helper.expiresAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    @SerialName("provider_token") val providerToken: String?,

    @SerialName("access_token") val accessToken: String,

    /**
     * The number of seconds until the token expires (since it was issued). Returned when a login is confirmed.
     */
    @SerialName("expires_in") val expiresIn: Long?,

    @SerialName("refresh_token") val refreshToken: String?,

    @SerialName("token_type") val tokenType: String,

    val user: User?
) {

    /**
     * A timestamp of when the token will expire. Returned when a login is confirmed.
     */
    val expiresAt: Long?
        get() = expiresIn?.let { expiresAt(it) }
}