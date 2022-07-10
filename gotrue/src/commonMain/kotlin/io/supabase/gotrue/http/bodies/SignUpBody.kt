package io.supabase.gotrue.http.bodies

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SignUpEmailBody(
    val email: String,
    val password: String,
    val data: JsonObject?,
    @SerialName("gotrue_meta_security") val goTrueMetaSecurity: GoTrueMetaSecurity
) {
    constructor(
        email: String,
        password: String,
        data: JsonObject?,
        captchaToken: String?
    ) : this(
        email = email,
        password = password,
        data = data,
        goTrueMetaSecurity = GoTrueMetaSecurity(hCaptchaToken = captchaToken)
    )
}

@Serializable
data class GoTrueMetaSecurity(
    @SerialName("hcaptcha_token") val hCaptchaToken: String?
)

@Serializable
data class SignUpPhoneBody(
    val phone: String,
    val password: String,
    val data: JsonObject?,
    @SerialName("gotrue_meta_security") val goTrueMetaSecurity: GoTrueMetaSecurity
) {
    constructor(
        phone: String,
        password: String,
        data: JsonObject?,
        captchaToken: String?
    ) : this(
        phone = phone,
        password = password,
        data = data,
        goTrueMetaSecurity = GoTrueMetaSecurity(hCaptchaToken = captchaToken)
    )
}
