package io.supabase.gotrue.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class User(
    val id: String,
    val app_metadata: AppMetadata,
    val user_metadata: Map<String, JsonElement>,
    val aud: String,
    val confirmation_sent_at: String?,
    val recovery_sent_at: String?,
    val action_link: String?,
    val email: String?,
    val phone: String?,
    val created_at: String,
    val confirmed_at: String?,
    val email_confirmed_at: String?,
    val phone_confirmed_at: String?,
    val last_sign_in_at: String?,
    val role: String?,
    val updated_at: String?
)

@Serializable
data class AppMetadata(
    val provider: String?,
    val metadata: Map<String, JsonElement>
)
