package io.supabase.gotrue.http

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Default implementation of the [GoTrueHttpClient] used by the GoTrueDefaultClient.
 *
 * Uses closable apache HTTP-Client 5.x.
 */
class GoTrueHttpClientKtor(
    private val url: String,
    private val globalHeaders: Map<String, String>,
    private val httpClient: () -> HttpClient
) : GoTrueHttpClient {

    override suspend fun post(path: String, headers: Map<String, String>, data: @Serializable Any?): String? {
        return httpClient().post(url + path) {
            headers {
                globalHeaders.forEach { header ->
                    append(header.key, header.value)
                }
                headers.forEach { header ->
                    append(header.key, header.value)
                }
            }
            body = Json.encodeToString(data)
        }
    }

    override suspend fun put(path: String, headers: Map<String, String>, data: @Serializable Any): String {
        return httpClient().put(url + path) {
            headers {
                globalHeaders.forEach { header ->
                    append(header.key, header.value)
                }
                headers.forEach { header ->
                    append(header.key, header.value)
                }
            }
            body = Json.encodeToString(data)
        }
    }

    override suspend fun get(path: String, headers: Map<String, String>): String {
        return httpClient().get(url + path) {
            headers {
                globalHeaders.forEach { header ->
                    append(header.key, header.value)
                }
                headers.forEach { header ->
                    append(header.key, header.value)
                }
            }
        }
    }
}