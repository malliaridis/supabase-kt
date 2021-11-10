package io.supabase.gotrue.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val json = Json
inline fun <reified T : Any> deserialize(content: String): T = json.decodeFromString(content)