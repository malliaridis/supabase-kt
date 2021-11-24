package io.supabase.gotrue.http

import io.supabase.gotrue.http.errors.ApiError
import io.supabase.gotrue.types.Subscription

data class SubscriptionResponse(
    val data: Subscription? = null,
    val error: ApiError? = null,
)