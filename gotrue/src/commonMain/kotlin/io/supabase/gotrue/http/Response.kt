package io.supabase.gotrue.http

import io.supabase.gotrue.types.Session
import io.supabase.gotrue.types.User
import kotlinx.serialization.Serializable

sealed interface Response {

    @Serializable
    data class ErrorResponse(val data: Unit = Unit, val error: ApiError) : Response

    @Serializable
    data class SessionResponse(val session: Session) : Response

    @Serializable
    data class DataSessionResponse(val data: Session) : Response

    @Serializable
    data class UserSessionResponse(val user: User? = null, val data: Session? = null) : Response

    @Serializable
    data class UserResponse(val data: User? = null) : Response
}
