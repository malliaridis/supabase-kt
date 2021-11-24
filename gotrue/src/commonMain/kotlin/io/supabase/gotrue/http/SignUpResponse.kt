package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.http.errors.ApiError

data class SignUpResponse(
    val user: UserInfo?,
    val session: Session?,
    val error: ApiError? = null,
    val data: Any?
)
