package io.supabase.gotrue.http

import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.http.errors.ApiError

data class UserUpdateResponse(
    val data: UserInfo? = null,
    val user: UserInfo? = null,
    val error: ApiError? = null
)