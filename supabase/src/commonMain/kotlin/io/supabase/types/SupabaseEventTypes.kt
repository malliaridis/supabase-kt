package io.supabase.types

enum class SupabaseEventTypes {
    INSERT,
    UPDATE,
    DELETE;
    // `*` TODO See how to handle star event type

    val isInsertOrUpdate
        get() = this == INSERT || this == UPDATE
    val isUpdateOrDelete
        get() = this == UPDATE || this == DELETE
}