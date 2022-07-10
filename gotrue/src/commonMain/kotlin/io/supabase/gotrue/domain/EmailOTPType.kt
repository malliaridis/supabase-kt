package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
enum class EmailOTPType {
    signup,
    invite,
    magiclink,
    recovery,
    email_change
}