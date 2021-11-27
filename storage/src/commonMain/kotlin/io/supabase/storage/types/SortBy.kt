package io.supabase.storage.types

import kotlinx.serialization.Serializable

@Serializable
data class SortBy(
    val column: String?,
    val order: String?
)