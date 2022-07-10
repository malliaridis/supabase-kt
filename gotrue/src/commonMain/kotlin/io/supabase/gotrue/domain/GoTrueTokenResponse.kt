package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
data class GoTrueTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String
)