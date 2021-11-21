package io.supabase.gotrue.http

import io.supabase.gotrue.types.Session
import io.supabase.gotrue.types.User

data class SessionResponse(
    val data: Session? = null,
    val user: User? = null,
    val error: ApiError? = null,
)

data class SimpleSessionResponse(
    val session: Session? = null,
    val error: ApiError? = null,
)