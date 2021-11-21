package io.supabase.gotrue.types

enum class AuthChangeEvent {
    SIGNED_IN,
    SIGNED_OUT,
    USER_UPDATED,
    USER_DELETED,
    PASSWORD_RECOVERY
}