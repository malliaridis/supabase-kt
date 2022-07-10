package io.supabase.gotrue.http

import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.errors.ApiError

data class UserUpdateResponse(
    val data: User? = null,
    val user: User? = null,
    val error: ApiError? = null
)