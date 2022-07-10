package io.supabase.gotrue.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserIdentity(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("identity_data") val identityData: Map<String, JsonElement>,
    val provider: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_sign_in_at") val lastSignInAt: String,
    @SerialName("updated_at") val updatedAt: String?
)