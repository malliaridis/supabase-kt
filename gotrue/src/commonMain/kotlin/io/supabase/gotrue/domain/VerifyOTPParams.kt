package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class VerifyOTPParams {

    @Serializable
    data class VerifyMobileOTPParams(
        val phone: String,
        val token: String,
        val type: MobileOTPType?
    ) : VerifyOTPParams()

    @Serializable
    data class VerifyEmailOTPParams(
        val email: String,
        val token: String,
        val type: EmailOTPType
    ) : VerifyOTPParams()
}

