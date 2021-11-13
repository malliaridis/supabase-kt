package io.supabase.storage.types

data class FileOptions(
    val cacheControl: String?,
    val contentType: String?,
    val upsert: Boolean?
)
