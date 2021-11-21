package io.supabase.gotrue.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserAttributes(
    /**
     * The user's email.
     */
    val email: String?,

    /**
     * The user's password.
     */
    val password: String?,

    /**
     * An email change token.
     */
    val email_change_token: String?,

    /**
     * A custom data object. Can be any JSON.
     */
    val data: JsonObject?
)
