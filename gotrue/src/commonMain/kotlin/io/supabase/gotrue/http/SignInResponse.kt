package io.supabase.gotrue.http

import io.supabase.gotrue.ApiError
import io.supabase.gotrue.domain.Provider
import io.supabase.gotrue.types.Session
import io.supabase.gotrue.types.User

// TODO Use sealed class instead for error and success
data class SignInResponse(
    val session: Session? = null,
    val user: User? = null,
    val provider: Provider? = null,
    val url: String? = null,
    val error: ApiError? = null,
    val data: Session? = null
)
