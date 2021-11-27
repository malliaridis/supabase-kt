package io.supabase.storage.types

import kotlinx.serialization.Serializable

@Serializable
data class SearchOptions(

    /**
     * The folder path.
     */
    val prefix: String = "",

    /**
     * The number of files you want to be returned.
     */
    val limit: Long = 100,

    /**
     * The starting position.
     */
    val offset: Long = 0,

    /**
     * The column to sort by. Can be any column inside a FileObject.
     */
    val sortBy: SortBy = SortBy(
        column = "name",
        order = "asc"
    )
)
