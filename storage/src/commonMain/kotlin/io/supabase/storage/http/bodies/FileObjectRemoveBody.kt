package io.supabase.storage.http.bodies

import kotlinx.serialization.Serializable

@Serializable
data class FileObjectRemoveBody(
    val prefixes: List<String>
)