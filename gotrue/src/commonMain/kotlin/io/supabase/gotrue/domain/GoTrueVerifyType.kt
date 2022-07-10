package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
enum class GoTrueVerifyType {
    SIGNUP,
    RECOVERY
}