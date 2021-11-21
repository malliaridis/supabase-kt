package io.supabase.gotrue.http.bodies

import kotlinx.serialization.Serializable

@Serializable
data class SignInEmailBody(
    val email: String,
    val password: String
)

@Serializable
data class SignInPhoneBody(
    val phone: String,
    val password: String
)