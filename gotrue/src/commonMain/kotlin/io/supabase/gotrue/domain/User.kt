package io.supabase.gotrue.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * @property userMetadata A user-defined key-value list.
 */
@Serializable
data class User(
    val id: String,
    @SerialName("app_metadata") val appMetadata: AppMetadata,
    @SerialName("user_metadata") val userMetadata: Map<String, JsonElement>,
    val aud: String,
    @SerialName("confirmation_sent_at") val confirmationSentAt: String? = null,
    @SerialName("recovery_sent_at") val recoverySentAt: String? = null,
    @SerialName("email_change_sent_at") val emailChangeSentAt: String? = null,
    @SerialName("new_email") val newEmail: String? = null,
    @SerialName("invited_at") val invitedAt: String? = null,
    @SerialName("action_link") val actionLink: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("email_confirmed_at") val emailConfirmedAt: String? = null,
    @SerialName("phone_confirmed_at") val phoneConfirmedAt: String? = null,
    @SerialName("last_sign_in_at") val lastSignInAt: String? = null,
    val role: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val identities: List<UserIdentity>?
)
