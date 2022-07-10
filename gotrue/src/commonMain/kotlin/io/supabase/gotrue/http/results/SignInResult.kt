package io.supabase.gotrue.http.results

import io.supabase.gotrue.domain.Provider
import io.supabase.gotrue.domain.Session
import io.supabase.gotrue.domain.User
import io.supabase.gotrue.http.errors.ApiError
import kotlinx.serialization.Serializable

@Serializable
sealed class SignInResult {

    @Serializable
    data class Success(
        val session: Session? = null,
        val user: User? = null,
        val provider: Provider? = null,
        val url: String? = null,
        val error: ApiError? = null
    ) : SignInResult()

    @Serializable
    data class Failure(val error: ApiError) : SignInResult()
}
