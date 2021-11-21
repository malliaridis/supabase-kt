package io.supabase.gotrue.types

data class VerifyOTPParams(
    val phone: String,
    val token: String
)

