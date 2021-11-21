package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
enum class MagicLinkType {
    signup,
    magiclink,
    recovery,
    invite
}