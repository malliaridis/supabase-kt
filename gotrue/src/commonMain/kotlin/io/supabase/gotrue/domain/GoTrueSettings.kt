package io.supabase.gotrue.domain

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