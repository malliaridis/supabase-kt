package io.supabase.gotrue.http

import io.supabase.gotrue.ApiError
import io.supabase.gotrue.types.Session

data class RefreshTokenResponse(
    val data: Session? = null,
    val error: ApiError? = null
)
