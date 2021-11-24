package io.supabase.gotrue.http.results

import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.http.errors.ApiError
import kotlinx.serialization.Serializable

@Serializable
sealed class UserResult {

    @Serializable
    data class Success(val data: UserInfo) : UserResult()

    @Serializable
    data class Failure(val error: ApiError) : UserResult()
}

@Serializable
sealed class UserDataResult {

    @Serializable
    data class Success(val user: UserInfo, val data: UserInfo) : UserDataResult()

    @Serializable
    data class Failure(val error: ApiError) : UserDataResult()
}