package io.supabase.gotrue.types

import io.supabase.gotrue.domain.Session
import kotlinx.serialization.Serializable

@Serializable
data class DataSession(
    val currentSession: Session?,
    val expiresAt: Long?
)
