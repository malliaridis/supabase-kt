package io.supabase.gotrue.types

data class UserCredentials(
    val email: String?,
    val phone: String?,
    val password: String?,
    val refreshToken: String?,
    /**
     * The name of the provider.
     */
    val provider: Provider?
)