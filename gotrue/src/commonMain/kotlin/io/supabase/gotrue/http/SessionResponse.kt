package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.errors.ApiError

data class SessionResponse(
    val data: Session? = null,
    val user: User? = null,
    val error: ApiError? = null,
)

data class SimpleSessionResponse(
    val session: Session? = null,
    val error: ApiError? = null,
)