package io.supabase.gotrue

import io.ktor.client.*
import io.supabase.gotrue.http.GoTrueHttpClient
import io.supabase.gotrue.http.GoTrueHttpClientKtor

/**
 * The default client uses Apache HTTP client 5.x and Jackson FasterXML for DTO conversion.
 *
 * If you want to customize, implement [GoTrueHttpClient].
 */
class GoTrueDefaultClient(
    url: String,
    headers: Map<String, String>
) : GoTrueClient(
    goTrueHttpClient = GoTrueHttpClientKtor(
        url = url,
        globalHeaders = headers,
        httpClient = { HttpClient() }
    )
)