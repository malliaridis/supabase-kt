package io.supabase.gotrue.http.results

import io.supabase.gotrue.http.errors.ApiError
import kotlinx.serialization.Serializable

@Serializable
sealed class EmptyResult {

    @Serializable
    class Success : EmptyResult()

    @Serializable
    data class Failure(val error: ApiError) : EmptyResult()
}

typealias MagicLinkResult = EmptyResult

typealias MobileOTPResult = EmptyResult

typealias ResetPasswordResult = EmptyResult