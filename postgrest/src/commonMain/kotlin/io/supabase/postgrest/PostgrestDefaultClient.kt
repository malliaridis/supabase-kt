package io.supabase.postgrest

import io.ktor.client.*
import io.ktor.http.*
import io.supabase.postgrest.http.PostgrestHttpClient
import io.supabase.postgrest.http.PostgrestHttpClientKtor

/**
 * The default client uses Apache HTTP client 5.x and Jackson FasterXML for DTO conversion.
 *
 * If you want to customize, implement [PostgrestHttpClient].
 *
 * @param[url] URL of the PostgREST endpoint.
 * @param[headers] Custom headers.
 * @param[schema] Postgres schema to switch to.
 */
class PostgrestDefaultClient(
    url: Url,
    headers: Map<String, String> = emptyMap(),
    schema: String? = null
) : PostgrestClient(
    url = url,
    headers = headers.toMutableMap(),
    schema = schema,
    httpClient = PostgrestHttpClientKtor(httpClient = { HttpClient() })
)
