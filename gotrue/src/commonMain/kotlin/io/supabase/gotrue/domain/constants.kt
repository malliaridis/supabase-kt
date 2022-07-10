package io.supabase.gotrue.domain

private const val version = "0.0.0"

const val GOTRUE_URL = "http://localhost:9999"

const val AUDIENCE = ""

val DEFAULT_HEADERS = mapOf("X-Client-Info" to "gotrue-js/${version}")

const val EXPIRY_MARGIN = 10 // in seconds

const val STORAGE_KEY = "supabase.auth.token"

/**
 * Former known as COOKIE_OPTIONS
 */
val DEFAULT_COOKIES = CookieOptions(
    name = "sb:token",
    lifetime = 60 * 60 * 8,
    domain = "",
    path = "/",
    sameSite = "lax",
)