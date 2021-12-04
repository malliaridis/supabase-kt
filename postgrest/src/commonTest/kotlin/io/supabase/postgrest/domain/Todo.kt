package io.supabase.postgrest.domain

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: String,
    val text: String
)
