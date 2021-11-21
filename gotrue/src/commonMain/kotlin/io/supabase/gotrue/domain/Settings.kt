package io.supabase.gotrue.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Settings(
    val external: Map<Provider, Boolean>,
    @SerialName("external_labels") val externalLabels: JsonElement, // TODO Find out what external labels are
    @SerialName("disable_signup") val disableSignup: Boolean,
    @SerialName("mailer_autoconfirm") val mailerAutoConfirm: Boolean,
    @SerialName("phone_autoconfirm") val phoneAutoConfirm: Boolean,
    @SerialName("sms_provider") val smsProvider: SMSProvider
)
