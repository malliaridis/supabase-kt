package io.supabase.storage.types

data class SearchOptions(

    /**
     * The number of files you want to be returned.
     */
    val limit: Long?,

    /**
     * The starting position.
     */
    val offset: Long?,

    /**
     * The column to sort by. Can be any column inside a FileObject.
     */
    val sortBy: SortBy?
)
