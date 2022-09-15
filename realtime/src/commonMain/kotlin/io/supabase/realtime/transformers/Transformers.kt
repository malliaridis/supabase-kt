package io.supabase.realtime.transformers

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class ConvertChangeDataOptions(
    val skipTypes: List<String>?
)

/**
 * Takes an array of columns and an object of string values then converts each string value
 * to its mapped type.
 *
 * @param columns
 * @param record
 * @param options The map of various options that can be applied to the mapper. Includes the array of types that should
 * not be converted.
 *
 * @example convertChangeData([{name: 'first_name', type: 'text'}, {name: 'age', type: 'int4'}], {first_name: 'Paul', age:'33'}, {})
 * //=>{ first_name: 'Paul', age: 33 }
 */
fun convertChangeData(
    columns: List<Column>,
    record: Record,
    options: ConvertChangeDataOptions?
): Record {
    val skipTypes = options?.skipTypes ?: emptyList()

    // TODO Implement me if necessary
//    record.forEach { entry ->
//        val column = columns.first { column -> column.name == entry.key }
//
//    }
//    record.keys.reduce<String, Record> { acc, recKey ->
//        acc[recKey] = convertColumn(recKey, columns, record, skipTypes)
//        acc
//    }
    return record
}

/**
 * Converts the value of an individual column.
 *
 * @param columnName The column that you want to convert
 * @param columns All the columns
 * @param record The map of string values
 * @param skipTypes An array of types that should not be converted
 * @return Useless information
 *
 * @example convertColumn('age', [{name: 'first_name', type: 'text'}, {name: 'age', type: 'int4'}], {first_name: 'Paul', age: '33'}, [])
 * //=> 33
 * @example convertColumn('age', [{name: 'first_name', type: 'text'}, {name: 'age', type: 'int4'}], {first_name: 'Paul', age: '33'}, ['int4'])
 * //=> "33"
 * TODO Implement me if necessary
 */
//fun convertColumn(
//    columnName: String,
//    columns: List<Column>,
//    record: Record,
//    skipTypes: List<String>
//): RecordValue {
//    val column = columns.find { x -> x.name == columnName }
//    val colType = column?.type
//    val value = record[columnName]
//
//    if (colType && !skipTypes.includes(colType)) {
//        return convertCell(colType, value)
//    }
//
//    return value // noop(value)
//}


/**
 * If the value of the cell `null`, returns null.
 * Otherwise, converts the string value to the correct type.
 * @param {String} type A postgres column type
 * @param {String} stringValue The cell value
 *
 * @example convertCell('bool', 't')
 * //=> true
 * @example convertCell('int8', '10')
 * //=> 10
 * @example convertCell('_int4', '{1,2,3,4}')
 * //=> [1,2,3,4]
 */
//fun convertCell(type: String, value: RecordValue): RecordValue? {
//    // if data type an array
//    if (type[0] == '_') {
//        val dataType = type.substring(1, type.length)
//        return toArray(value, dataType)
//    }
//    // If not null, convert to correct type.
//    when (PostgresType.valueOf(type)) {
//        PostgresType.bool -> return toBoolean(value)
//        PostgresType.float4,
//        PostgresType.float8,
//        PostgresType.int2,
//        PostgresType.int4,
//        PostgresType.int8,
//        PostgresType.numeric,
//        PostgresType.oid -> return toNumber(value)
//        PostgresType.json,
//        PostgresType.jsonb -> return toJson(value)
//        PostgresType.timestamp -> return toTimestampString(value) // Format to be consistent with PostgREST
//        PostgresType.abstime, // To allow users to cast it based on Timezone
//        PostgresType.date, // To allow users to cast it based on Timezone
//        PostgresType.daterange,
//        PostgresType.int4range,
//        PostgresType.int8range,
//        PostgresType.money,
//        PostgresType.reltime, // To allow users to cast it based on Timezone
//        PostgresType.text,
//        PostgresType.time, // To allow users to cast it based on Timezone
//        PostgresType.timestamptz, // To allow users to cast it based on Timezone
//        PostgresType.timetz, // To allow users to cast it based on Timezone
//        PostgresType.tsrange,
//        PostgresType.tstzrange -> return noop(value)
//        else -> return noop(value) // Return the value for remaining types
//    }
//}

private fun noop(value: RecordValue): RecordValue {
    return value
}

fun toBoolean(value: RecordValue): Boolean? {
    return when (value.toString()) {
        "t", "true", "1" -> true
        "f", "false", "0" -> false
        else -> value as? Boolean
    }
}

fun toNumber(value: RecordValue): Float? {
    return (value as? String)?.let {
        val parsedValue = value.toFloatOrNull()
        if (parsedValue != null && !parsedValue.isNaN()) {
            return@let parsedValue
        } else null
    } ?: value as? Float
}

// TODO Change return type to JsonElement or JsonObject
fun toJson(value: RecordValue): RecordValue {
    return (value as? String)?.let {
        return try {
            Json.decodeFromString<JsonObject>(value)
        } catch (error: Error) {
            println("JSON parse error: ${error.message}")
            value
        }
    } ?: value
}


/**
 * Converts a Postgres Array into a native JS array
 *
 * @example toArray('{}', 'int4')
 * //=> []
 * @example toArray('{"[2021-01-01,2021-12-31)","(2021-01-01,2021-12-32]"}', 'daterange')
 * //=> ['[2021-01-01,2021-12-31)', '(2021-01-01,2021-12-32]']
 * @example toArray([1,2,3,4], 'int4')
 * //=> [1,2,3,4]
 */
//fun toArray(value: RecordValue, type: String): RecordValue {
//    if (value !is String) return value
//
//    val lastIdx = value.length - 1
//    val closeBrace = value[lastIdx]
//    val openBrace = value[0]
//
//    // Confirm value is a Postgres array by checking curly brackets
//    if (openBrace == '{' && closeBrace == '}') {
//
//        val valTrim = value.substring(1, lastIdx)
//
//        // TODO: find a better solution to separate Postgres array data
//        val arr: List<Any> = try {
//            Json.parse("[$valTrim]")
//        } catch (error: Error) {
//            // WARNING: splitting on comma does not cover all edge cases
//            valTrim.split(",")
//        }
//
//        return arr.map { base: BaseValue -> convertCell(type, base) }
//    }
//
//    return value
//}

/**
 * Fixes timestamp to be ISO-8601. Swaps the space between the date and time for a "T".
 * See https://github.com/supabase/supabase/issues/18
 *
 * @example toTimestampString('2019-09-10 00:00:00')
 * //=> '2019-09-10T00:00:00'
 */
fun toTimestampString(value: RecordValue): RecordValue {
    if (value is String) {
        return value.replace(' ', 'T')
    }

    return value
}
