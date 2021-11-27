package io.supabase.storage.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FileObject(
    val name: String,
    val bucket_id: String,
    val owner: String,
    val id: String,
    val updated_at: String,
    val created_at: String,
    val last_accessed_at: String,
    val metadata: JsonElement,
    val buckets: Bucket
)

