package io.supabase.gotrue.domain

import kotlinx.datetime.UtcOffset
import kotlinx.serialization.Serializable

@Serializable
data class GoTrueUserResponse(
    val id: String,
    val email: String,
    val confirmationSentAt: UtcOffset,
    val createdAt: UtcOffset,
    val updatedAt: UtcOffset
)