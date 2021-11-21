package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Provider {
    apple,
    azure,
    bitbucket,
    discord,
    email,
    facebook,
    github,
    gitlab,
    google,
    phone,
    saml,
    slack,
    spotify,
    twitch,
    twitter
}

@Serializable
enum class SMSProvider {
    twilio
}