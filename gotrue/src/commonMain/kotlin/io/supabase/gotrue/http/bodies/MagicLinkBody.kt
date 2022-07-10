package io.supabase.gotrue.http.bodies

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MagicLinkEmailBody(
    val email: String,
    @SerialName("create_user") val createUser: Boolean,
    @SerialName("gotrue_meta_security") val goTrueMetaSecurity: GoTrueMetaSecurity
) {
    constructor(
        email: String,
        createUser: Boolean,
        captchaToken: String?
    ) : this(
        email = email,
        createUser = createUser,
        goTrueMetaSecurity = GoTrueMetaSecurity(hCaptchaToken = captchaToken)
    )
}

@Serializable
data class MagicLinkGenerationBody(
    val type: MagicLinkType,
    val email: String,
    val password: String?,
    val data: JsonElement?,
    @SerialName("redirect_to")
    val redirectTo: String?
)

@Serializable
enum class MagicLinkType {
    signup,
    magiclink,
    recovery,
    invite
}