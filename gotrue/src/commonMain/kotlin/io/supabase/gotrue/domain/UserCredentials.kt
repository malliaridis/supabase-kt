package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

/**
 * @property email The user's email.
 * @property phone The user's phone.
 * @property password The user's password.
 * @property provider The name of the provider.
 * @property oidc The OpenID Connect credentials.
 */
@Serializable
data class UserCredentials(
    val email: String?,
    val phone: String?,
    val password: String?,
    val refreshToken: String?,
    val provider: Provider?,
    val oidc: OpenIDConnectCredentials?
)