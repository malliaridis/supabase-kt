package io.supabase.gotrue.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * @property email The user's email.
 * @property phone The user's phone.
 * @property password The user's password.
 * @property emailChangeToken An email change token.
 * @property data A custom data object. Can be any JSON.
 */
interface IUserAttributes {
    val email: String?
    val phone: String?
    val password: String?
    val emailChangeToken: String?
    val data: JsonObject?
}

@Serializable
data class UserAttributes(
    override val email: String?,
    override val phone: String?,
    override val password: String?,
    @SerialName("email_change_token") override val emailChangeToken: String?,
    override val data: JsonObject?
) : IUserAttributes

/**
 * @property userMetadata A custom data object for user_metadata.
 *
 * Can be any JSON.
 *
 * Only a service role can modify.
 *
 * Note: When using the GoTrueAdminApi and wanting to modify a user's user_metadata,
 * this attribute is used instead of UserAttributes data.
 *
 * @property appMetadata A custom data object for app_metadata that.
 *
 * Only a service role can modify.
 *
 * Can be any JSON that includes app-specific info, such as identity providers, roles, and other
 * access control information.
 *
 * @property emailConfirm Sets if a user has confirmed their email address.
 *
 * Only a service role can modify.
 *
 * @property phoneConfirm Sets if a user has confirmed their phone number.
 *
 * Only a service role can modify.
 */
@Serializable
data class AdminUserAttributes(
    override val email: String?,
    override val phone: String?,
    override val password: String?,
    @SerialName("email_change_token") override val emailChangeToken: String?,
    override val data: JsonObject?,
    @SerialName("user_metadata") val userMetadata: JsonObject?,
    @SerialName("app_metadata") val appMetadata: JsonObject?,
    @SerialName("email_confirm") val emailConfirm: Boolean?,
    @SerialName("phone_confirm") val phoneConfirm: Boolean?
) : IUserAttributes