package io.supabase.gotrue.domain

import io.supabase.gotrue.helper.expiresAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    /**
     * JWT representing the user
     */
    @SerialName("access_token") val accessToken: String,

    /**
     * The number of seconds until the token expires (since it was issued). Returned when a login is confirmed.
     */
    @SerialName("expires_in") val expiresIn: Long? = null,

    @SerialName("refresh_token") val refreshToken: String? = null,

    val scope: String? = null,

    @SerialName("token_type") val tokenType: String,

    @SerialName("id_token") val idToken: String? = null,

    @SerialName("provider_token") val providerToken: String? = null,

    val type: MagicLinkType? = null,

    val user: UserInfo?
) {

    /**
     * A timestamp of when the token will expire. Returned when a login is confirmed.
     */
    val expiresAt: Long?
        get() = expiresIn?.let { expiresAt(it) }
}