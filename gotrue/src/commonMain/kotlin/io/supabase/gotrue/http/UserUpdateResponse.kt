package io.supabase.gotrue.http

import io.supabase.gotrue.types.User

data class UserUpdateResponse(
    val data: User? = null,
    val user: User? = null,
    val error: ApiError? = null
)