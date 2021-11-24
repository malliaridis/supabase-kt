package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.http.errors.ApiError
import kotlinx.serialization.Serializable

sealed interface Response {

    @Serializable
    data class ErrorResponse(val data: Unit = Unit, val error: ApiError) : Response

    @Serializable
    data class SessionResponse(val session: Session) : Response

    @Serializable
    data class DataSessionResponse(val data: Session) : Response

    @Serializable
    data class UserSessionResponse(val user: UserInfo? = null, val data: Session? = null) : Response

    @Serializable
    data class UserResponse(val data: UserInfo? = null) : Response
}
