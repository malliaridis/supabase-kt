package io.supabase.gotrue.types

import kotlinx.serialization.Serializable

@Serializable
enum class MagicLinkType {
    signup,
    magiclink,
    recovery,
    invite
}