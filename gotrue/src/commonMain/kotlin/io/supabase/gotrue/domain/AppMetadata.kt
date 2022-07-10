package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AppMetadata(
    val provider: String? = null,
    val providers: Map<String, JsonElement>? = null
)