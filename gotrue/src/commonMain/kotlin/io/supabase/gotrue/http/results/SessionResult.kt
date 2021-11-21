package io.supabase.gotrue.http.results

import io.supabase.gotrue.ApiError
import io.supabase.gotrue.types.Session
import kotlinx.serialization.Serializable

@Serializable
sealed class SessionResult {

    @Serializable
    data class Success(val data: Session) : SessionResult()

    @Serializable
    data class Failure(val error: ApiError) : SessionResult()
}
