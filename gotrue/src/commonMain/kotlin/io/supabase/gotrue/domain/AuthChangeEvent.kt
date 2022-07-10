package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AuthChangeEvent {
    SIGNED_IN,
    SIGNED_OUT,
    USER_UPDATED,
    USER_DELETED,
    TOKEN_REFRESHED,
    PASSWORD_RECOVERY
}