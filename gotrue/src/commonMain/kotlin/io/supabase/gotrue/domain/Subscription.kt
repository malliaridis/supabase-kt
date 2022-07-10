package io.supabase.gotrue.domain

import kotlinx.serialization.Serializable

/**
 * @property id The subscriber UUID. This will be set by the client.
 * @property callback The function to call every time there is an event. eg: (eventName) => {}
 * @property unsubscribe Call this to remove the listener.
 */
@Serializable
data class Subscription(
    val id: String,
    val callback: (event: AuthChangeEvent, session: Session?) -> Unit,
    val unsubscribe: () -> Unit
)