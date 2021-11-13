package io.supabase.storage.types

data class Bucket(
    val id: String,
    val name: String,
    val owner: String,
    val created_at: String,
    val updated_at: String,
    val public: Boolean
)