package io.supabase.gotrue.http

import io.supabase.gotrue.domain.Subscription
import io.supabase.gotrue.http.errors.ApiError

data class SubscriptionResponse(
    val data: Subscription? = null,
    val error: ApiError? = null,
)