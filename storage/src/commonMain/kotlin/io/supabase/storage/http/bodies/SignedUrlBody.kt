package io.supabase.storage.http.bodies

import kotlinx.serialization.Serializable

@Serializable
data class SignedUrlBody(
    val expiresIn: Long
)