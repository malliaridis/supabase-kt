package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.http.errors.ApiError

data class RefreshTokenResponse(
    val data: Session? = null,
    val error: ApiError? = null
)
