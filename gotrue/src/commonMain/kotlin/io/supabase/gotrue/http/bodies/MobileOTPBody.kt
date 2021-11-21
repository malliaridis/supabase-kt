package io.supabase.gotrue.http.bodies

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileOTPBody(
    val phone: String
)

@Serializable
data class VerifyMobileOTPBody(
    val phone: String,
    val token: String,
    val type: String,
    @SerialName("redirect_to")
    val redirectTo: String?
)