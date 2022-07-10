package io.supabase.gotrue.http.bodies

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileOTPBody(
    val phone: String,
    @SerialName("create_user") val createUser: Boolean,
    @SerialName("gotrue_meta_security") val goTrueMetaSecurity: GoTrueMetaSecurity
) {
    constructor(
        phone: String,
        createUser: Boolean,
        captchaToken: String?
    ) : this(
        phone = phone,
        createUser = createUser,
        goTrueMetaSecurity = GoTrueMetaSecurity(hCaptchaToken = captchaToken)
    )
}

@Serializable
data class VerifyMobileOTPBody(
    val phone: String,
    val token: String,
    val type: String,
    @SerialName("redirect_to")
    val redirectTo: String?
)