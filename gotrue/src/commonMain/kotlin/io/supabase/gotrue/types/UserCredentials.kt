package io.supabase.gotrue.types

import io.supabase.gotrue.domain.Provider

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