package io.supabase.gotrue.http.bodies

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class EmailInviteBody(
    val email: String,
    val data: JsonElement?
)
