package io.supabase.gotrue.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true; isLenient = true }
inline fun <reified T : Any> deserialize(content: String): T = json.decodeFromString(content)