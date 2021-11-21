package io.supabase.gotrue.http.bodies

import io.supabase.gotrue.types.MagicLinkType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MagicLinkEmailBody(
    val email: String
)

@Serializable
data class MagicLinkGenerationBody(
    val type: MagicLinkType,
    val email: String,
    val password: String?,
    val data: JsonElement?,
    @SerialName("redirect_to")
    val redirectTo: String?
)