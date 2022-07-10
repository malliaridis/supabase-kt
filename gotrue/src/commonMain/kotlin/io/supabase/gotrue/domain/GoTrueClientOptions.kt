package io.supabase.gotrue.domain

import io.ktor.http.*

interface SupportedStorage {
    suspend fun getItem(key: String): String

    suspend fun setItem(key: String, item: String): String

    suspend fun removeItem(vararg args: Any): String
}

expect class LocalStorage() : SupportedStorage

data class GoTrueClientOptions(
    val url: String,
    val headers: Headers = headersOf(),
    val detectSessionInUrl: Boolean = true,
    val autoRefreshToken: Boolean = true,
    val persistSession: Boolean = true,
    val localStorage: SupportedStorage? = null,
    val cookieOptions: CookieOptions? = null
)