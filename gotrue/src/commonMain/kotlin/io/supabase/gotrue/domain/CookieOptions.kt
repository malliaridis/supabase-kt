package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

/**
 * @property name The Cookie name prefix. Defaults to `sb` meaning the cookies will be `sb-access-token` and
 * `sb-refresh-token`.
 * @property lifetime The cookie lifetime (expiration) in seconds. Set to 8 hours by default.
 * @property domain The cookie domain this should run on. Leave it blank to restrict it to your domain.
 * @property sameSite SameSite configuration for the session cookie. Defaults to `lax`, but can be changed to `strict`
 * or `none`. Set it to false if you want to disable the SameSite setting.
 */
@Serializable
data class CookieOptions(
    val name: String?,
    val lifetime: Long?,
    val domain: String?,
    val path: String?,
    val sameSite: String?
)
