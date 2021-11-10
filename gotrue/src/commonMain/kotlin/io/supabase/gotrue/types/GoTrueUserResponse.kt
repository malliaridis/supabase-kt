package io.supabase.gotrue.types

import kotlinx.datetime.UtcOffset

data class GoTrueUserResponse(
    val id: String,
    val email: String,
    val confirmationSentAt: UtcOffset,
    val createdAt: UtcOffset,
    val updatedAt: UtcOffset
)