package io.supabase.gotrue.http

import io.supabase.gotrue.ApiError
import io.supabase.gotrue.types.Session
import io.supabase.gotrue.types.User

data class SignUpResponse(
    val user: User?,
    val session: Session?,
    val error: ApiError? = null,
    val data: Any?
)
