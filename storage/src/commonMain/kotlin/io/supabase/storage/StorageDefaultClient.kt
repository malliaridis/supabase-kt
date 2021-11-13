package io.supabase.storage

import io.ktor.client.*
import io.supabase.storage.http.StorageHttpClientKtor

/**
 * The default client uses Ktor HTTP client with integrated JSON serialization.
 */
class StorageDefaultClient(
    url: String,
    headers: Map<String, String>
) : StorageClient(
    storageHttpClient = StorageHttpClientKtor(
        url = url,
        headers = headers,
        httpClient = { HttpClient() }
    )
)