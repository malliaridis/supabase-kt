package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
data class MagicLinkData(
    val action_link: String
    // TODO See if there are further information to be registered here
)
