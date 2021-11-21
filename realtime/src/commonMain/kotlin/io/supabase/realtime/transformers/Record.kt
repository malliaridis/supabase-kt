package io.supabase.realtime.transformers

import kotlinx.serialization.json.JsonPrimitive

/**
 * BaseValue that can be null, String, Number or Boolean
 */
typealias BaseValue = Any

/**
 * RecordValue that can be BaseValue or List<BaseValue>.
 */
typealias RecordValue = Any

typealias Record = Map<String, JsonPrimitive>