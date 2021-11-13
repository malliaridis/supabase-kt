package io.supabase.gotrue.types

interface SupportedStorage {
    suspend fun getItem(vararg args: Any): Any

    suspend fun setItem(vararg args: Any): Any

    suspend fun removeItem(vararg args: Any): Any
}

data class GoTrueClientOptions(
    val url: String?,
    val headers: Map<String, String>?,
    val detectSessionInUrl: Boolean?,
    val autoRefreshToken: Boolean?,
    val persistSession: Boolean?,
    val localStorage: SupportedStorage?,
    val cookieOptions: CookieOptions?,

    // TODO Replace with httpClient
    // fetch?: Fetch
)