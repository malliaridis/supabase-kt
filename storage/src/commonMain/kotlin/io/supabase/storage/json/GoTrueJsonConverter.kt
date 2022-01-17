package io.supabase.storage.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }
inline fun <reified T : Any> deserialize(content: String): T = json.decodeFromString(content)