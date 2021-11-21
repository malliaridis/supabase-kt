package io.supabase.gotrue.http.bodies

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SignUpEmailBody(
    val email: String,
    val password: String,
    val data: JsonElement?
)

@Serializable
data class SignUpPhoneBody(
    val phone: String,
    val password: String,
    val data: JsonElement?
)
