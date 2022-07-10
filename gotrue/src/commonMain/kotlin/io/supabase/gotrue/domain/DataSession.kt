package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
data class DataSession(
    val currentSession: Session?,
    val expiresAt: Long?
)
