package io.supabase.gotrue.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class User(
    val id: String,
    @SerialName("app_metadata") val appMetadata: AppMetadata,
    @SerialName("user_metadata") val userMetadata: Map<String, JsonElement>,
    val aud: String,
    @SerialName("confirmation_sent_at") val confirmationSentAt: String?,
    @SerialName("recovery_sent_at") val recoverySentAt: String?,
    @SerialName("action_link") val actionLink: String?,
    val email: String?,
    val phone: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("confirmed_at") val confirmedAt: String?,
    @SerialName("email_confirmed_at") val emailConfirmedAt: String?,
    @SerialName("phone_confirmed_at") val phoneConfirmedAt: String?,
    @SerialName("last_sign_in_at") val lastSignInAt: String?,
    val role: String?,
    @SerialName("updated_at") val updatedAt: String?
)

@Serializable
data class AppMetadata(
    val provider: String?,
    val metadata: Map<String, JsonElement>
)
