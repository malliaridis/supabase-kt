package io.supabase.storage.types

data class FileObject(
    val name: String,
    val bucket_id: String,
    val owner: String,
    val id: String,
    val updated_at: String,
    val created_at: String,
    val last_accessed_at: String,
    val metadata: Any,
    val buckets: Bucket
)

