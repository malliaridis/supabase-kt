package io.supabase.gotrue.types

import kotlinx.serialization.Serializable

@Serializable
data class DataSession(
    val currentSession: Session?,
    val expiresAt: Long?
)
