package io.supabase.gotrue.types

import kotlinx.serialization.Serializable

@Serializable
data class GoTrueSettings(
    val external: External,
    val disableSignup: Boolean,
    val autoconfirm: Boolean
) {

    @Serializable
    data class External(
        val bitbucket: Boolean,
        val github: Boolean,
        val gitlab: Boolean,
        val google: Boolean
    )
}