package io.supabase.postgrest.builder

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

open class PostgrestQueryBuilder<T : @Serializable Any>(
    url: String,
    defaultHeaders: Headers,
    schema: String = "public",
    httpClient: () -> HttpClient
) : PostgrestBuilder<T>(url, defaultHeaders, schema, httpClient) {

    companion object {
        const val HEADER_PREFER = "Prefer"
    }

    /**
     * Performs vertical filtering with SELECT.
     *
     * @param[columns] The columns to retrieve, separated by commas.
     * @param[head] When set to true, select will void data.
     * @param[count] Count algorithm to use to count rows in a table.
     */
    fun select(
        columns: String = "*",
        head: Boolean = false,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        if (head) {
            method = HttpMethod.Head
        } else {
            method = HttpMethod.Get
        }

        val cleanedColumns = cleanColumns(columns)

        setSearchParam("select", cleanedColumns)

        if (count != null) {
            setHeader(HEADER_PREFER, "count=${count.identifier}")
        }

        return PostgrestFilterBuilder(this)
    }

    /**
     * Performs an INSERT into the table.
     *
     * @param[values] The values to insert.
     * @param[upsert] If `true`, performs an UPSERT.
     * @param[onConflict] By specifying the `on_conflict` query parameter, you can make UPSERT work on a column(s) that has a UNIQUE constraint.
     * @param[returning] By default the new record is returned. Set this to 'minimal' if you don't need this value.
     */
    fun insert(
        values: List<T>,
        upsert: Boolean = false,
        onConflict: String? = null,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        method = HttpMethod.Post

        val preferHeaders = mutableListOf("return=${returning.identifier}")
        if (upsert) preferHeaders.add("resolution=merge-duplicates")

        if (upsert && onConflict != null) setSearchParam("on_conflict", onConflict)
        body = values

        if (count != null) {
            preferHeaders.add("count=${count.identifier}")
        }

        setHeader(HEADER_PREFER, preferHeaders.joinToString(","))

        return PostgrestFilterBuilder(this)
    }

    /**
     * Performs an INSERT into the table.
     *
     * @param[value] The value to insert.
     * @param[upsert] If `true`, performs an UPSERT.
     * @param[onConflict] By specifying the `on_conflict` query parameter, you can make UPSERT work on a column(s) that has a UNIQUE constraint.
     * @param[returning] By default the new record is returned. Set this to 'minimal' if you don't need this value.
     */
    fun insert(
        value: T,
        upsert: Boolean = false,
        onConflict: String? = null,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        return insert(listOf(value), upsert, onConflict, returning, count)
    }

    /**
     * Performs an UPDATE on the table.
     *
     * @param[value] The values to update.
     * @param[returning] By default the updated record is returned. Set this to 'minimal' if you don't need this value.
     */
    fun update(
        value: Any,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null
    ): PostgrestFilterBuilder<T> {
        method = HttpMethod.Patch
        body = value

        val prefersHeaders = mutableListOf("return=${returning.identifier}")

        if (count != null) {
            prefersHeaders.add("count=${count.identifier}")
        }
        setHeader(HEADER_PREFER, prefersHeaders.joinToString(","))

        return PostgrestFilterBuilder(this)
    }

    /**
     * Performs a DELETE on the table.
     *
     * @param[returning] If `true`, return the deleted row(s) in the response.
     */
    fun delete(returning: Returning = Returning.REPRESENTATION, count: Count? = null): PostgrestFilterBuilder<T> {
        method = HttpMethod.Delete

        val prefersHeaders = mutableListOf("return=${returning.identifier}")
        if (count != null) {
            prefersHeaders.add("count=${count.identifier}")
        }
        setHeader(HEADER_PREFER, prefersHeaders.joinToString(","))

        return PostgrestFilterBuilder(this)
    }
}

enum class Count(val identifier: String) {
    EXACT("exact"),
    PLANNED("planned"),
    ESTIMATED("estimated")
}

enum class Returning(val identifier: String) {
    MINIMAL("minimal"),
    REPRESENTATION("representation")
}