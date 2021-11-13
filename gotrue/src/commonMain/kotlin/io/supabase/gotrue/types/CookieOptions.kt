package io.supabase.gotrue.types

data class CookieOptions(

    /**
     * (Optional) The name of the cookie. Defaults to `sb:token`.
     */
    val name: String?,

    /**
     * (Optional) The cookie lifetime (expiration) in seconds. Set to 8 hours by default.
     */
    val lifetime: Long?,

    /**
     * (Optional) The cookie domain this should run on. Leave it blank to restrict it to your domain.
     */
    val domain: String?,

    val path: String?,

    /**
     * (Optional) SameSite configuration for the session cookie. Defaults to 'lax', but can be changed to 'strict' or 'none'. Set it to false if you want to disable the SameSite setting.
     */
    val sameSite: String?
)