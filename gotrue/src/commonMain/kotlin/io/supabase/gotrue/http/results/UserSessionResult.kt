package io.supabase.gotrue.http.results

import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.errors.ApiError
import kotlinx.serialization.Serializable

@Serializable
sealed class UserSessionResult {

    @Serializable
    data class UserSuccess(val data: User) : UserSessionResult()

    @Serializable
    data class SessionSuccess(val data: Session) : UserSessionResult()

    @Serializable
    data class Failure(val error: ApiError) : UserSessionResult()
}