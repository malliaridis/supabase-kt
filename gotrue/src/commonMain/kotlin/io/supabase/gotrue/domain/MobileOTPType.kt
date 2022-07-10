package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
enum class MobileOTPType {
    sms,
    phone_change
}