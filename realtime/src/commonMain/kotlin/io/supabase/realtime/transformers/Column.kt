package io.supabase.realtime.transformers

data class Column(

    /**
     * The column name. eg: "user_id"
     */
    val name: String,

    /**
     * The column type. eg: "uuid"
     */
    val type: PostgresType,

    /**
     * Any special flags for the column. eg: ["key"]
     */
    val flags: List<String>?,

    /**
     * The type modifier. eg: 4294967295
     */
    val type_modifier: Long?
)