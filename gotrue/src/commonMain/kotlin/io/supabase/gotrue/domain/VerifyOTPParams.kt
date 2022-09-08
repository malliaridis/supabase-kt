package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class VerifyOTPParams {

    /**
     * @property email The user's email address.
     * @property phone The user's phone number.
     * @property token The user's password.
     * @property type The user's verification type.
     */
    @Serializable
    data class VerifyMobileOTPParams(
        val email: String?,
        val phone: String,
        val token: String,
        val type: MobileOTPType = MobileOTPType.sms
    ) : VerifyOTPParams()

    /**
     * @property email The user's email address.
     * @property phone The user's phone number.
     * @property token The user's password.
     * @property type The user's verification type.
     */
    @Serializable
    data class VerifyEmailOTPParams(
        val email: String,
        val phone: String?,
        val token: String,
        val type: EmailOTPType
    ) : VerifyOTPParams()
}
