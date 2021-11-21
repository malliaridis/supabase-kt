package io.supabase.gotrue.types

import io.supabase.gotrue.helper.expiresAt
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val provider_token: String?,

    val access_token: String,

    /**
     * The number of seconds until the token expires (since it was issued). Returned when a login is confirmed.
     */
    val expires_in: Long?,

    val refresh_token: String?,

    val token_type: String,

    val user: User?
) {

    /**
     * A timestamp of when the token will expire. Returned when a login is confirmed.
     */
    val expires_at: Long?
        get() = expires_in?.let { expiresAt(it) }
}