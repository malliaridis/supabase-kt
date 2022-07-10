package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.errors.ApiError

data class SignUpResponse(
    val user: User?,
    val session: Session?,
    val error: ApiError? = null,
    val data: Any?
)
