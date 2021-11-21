package io.supabase.gotrue.types

data class Subscription(
    /**
     * The subscriber UUID. This will be set by the client.
     */
    val id: String,

    /**
     * The function to call every time there is an event. eg: (eventName) => {}
     */
    val callback: (event: AuthChangeEvent, session: Session?) -> Unit,

    /**
     * Call this to remove the listener.
     */
    val unsubscribe: () -> Unit
)