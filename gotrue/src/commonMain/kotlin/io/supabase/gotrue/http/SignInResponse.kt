package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Provider
import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.http.errors.ApiError

// TODO Use sealed class instead for error and success
data class SignInResponse(
    val session: Session? = null,
    val user: UserInfo? = null,
    val provider: Provider? = null,
    val url: String? = null,
    val error: ApiError? = null,
    val data: Session? = null
)
