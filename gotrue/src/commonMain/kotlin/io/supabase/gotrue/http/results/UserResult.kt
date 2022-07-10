package io.supabase.gotrue.http.results

import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.errors.ApiError
import kotlinx.serialization.Serializable

@Serializable
sealed class UserResult {

    @Serializable
    data class Success(val data: User) : UserResult()

    @Serializable
    data class Failure(val error: ApiError) : UserResult()
}

@Serializable
sealed class UserDataResult {

    @Serializable
    data class Success(val user: User, val data: User) : UserDataResult()

    @Serializable
    data class Failure(val error: ApiError) : UserDataResult()
}