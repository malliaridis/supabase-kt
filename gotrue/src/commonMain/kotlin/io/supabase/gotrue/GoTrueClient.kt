package io.supabase.gotrue

import io.ktor.client.utils.*
import io.ktor.http.*
import io.supabase.gotrue.domain.Provider
import io.supabase.gotrue.helper.*
import io.supabase.gotrue.http.*
import io.supabase.gotrue.http.results.EmptyResult
import io.supabase.gotrue.http.results.SessionResult
import io.supabase.gotrue.http.results.UserDataResult
import io.supabase.gotrue.http.results.UserSessionResult
import io.supabase.gotrue.types.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Create a new client for use in the browser.
 */
open class GoTrueClient(
    private val url: String = "http://localhost:9999",
    private val headers: Headers = buildHeaders { "X-Client-Info" to "gotrue-js/0.0.0" },
    private val detectSessionInUrl: Boolean = true,
    internal val autoRefreshToken: Boolean = true,
    internal val persistSession: Boolean = true,
    internal var localStorage: SupportedStorage? = TODO(),
    private val cookieOptions: CookieOptions = DEFAULT_COOKIES
) {

    /**
     * The currently logged in user or null.
     */
    internal var currentUser: User? = null

    /**
     * The session object for the currently logged in user or null.
     */
    internal var currentSession: Session? = null

    /**
     * Namespace for the GoTrue API methods.
     * These can be used for example to get a user from a JWT in a server environment or reset a user's password.
     */
    var api: GoTrueApi = GoTrueApi(
        url = url,
        headers = headers,
        cookieOptions = cookieOptions
    )

    internal var stateChangeEmitters: MutableMap<String, Subscription> = mutableMapOf()
    internal var refreshTokenTimer: Timer? = null

    fun initSync() {
        // TODO Run blocking initAsync
    }

    suspend fun initAsync() {
        recoverSession()
        recoverAndRefresh()

        // Handle the OAuth redirect
        if (detectSessionInUrl && isBrowser() && getParameterByName("access_token") != null) {
            val result = getSessionFromUrl(true)
            if (result.error != null) println("Error getting session from URL. ${result.error}")
        }
    }

    /**
     * Creates a new user.
     * @type UserCredentials
     * @param email The user's email address.
     * @param password The user's password.
     * @param phone The user's phone number.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param data Optional user metadata.
     */
    suspend fun signUp(
        email: String?,
        password: String?,
        phone: String?,
        redirectTo: String? = null,
        data: JsonElement? = null
    ): SignUpResponse {
        try {
            removeSession()

            val response: UserSessionResult = // TODO See if SignUpResponse is correct here
                if (phone != null && password != null) api.signUpWithPhone(phone, password, data)
                else api.signUpWithEmail(email!!, password!!, redirectTo, data)

            if (response is UserSessionResult.Failure) throw Exception(response.error.toString())

            var session: Session? = null
            val user: User?

            when (response) {
                is UserSessionResult.SessionSuccess -> {
                    session = response.data
                    user = session.user as User
                    saveSession(session)
                    notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                }
                is UserSessionResult.UserSuccess -> {
                    user = response.data
                }
                else -> throw Exception("An invalid state was reached (SignUpResponse)")
            }

            return SignUpResponse(data = data, user = user, session = session)
        } catch (e: Exception) {
            return SignUpResponse(data = null, user = null, session = null, error = ApiError(e.message!!, -1))
        }
    }

    /**
     * Log in an existing user, or login via a third-party provider.
     * @type UserCredentials
     * @param email The user's email address.
     * @param password The user's password.
     * @param refreshToken A valid refresh token that was returned on login.
     * @param provider One of the providers supported by GoTrue.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param scopes A space-separated list of scopes granted to the OAuth application.
     */
    suspend fun signIn(
        email: String?,
        phone: String?,
        password: String?,
        refreshToken: String?,
        provider: Provider?,
        redirectTo: String?,
        scopes: String?
    ): SignInResponse {
        try {
            removeSession()

            if (email != null && password == null) {
                val response = api.sendMagicLinkEmail(email, redirectTo)
                return SignInResponse(error = (response as? EmptyResult.Failure)?.error)
            }
            if (email != null && password != null) {
                return handleEmailSignIn(email, password, redirectTo)
            }
            if (phone != null && password == null) {
                val response = api.sendMobileOTP(phone)
                return SignInResponse(error = (response as? EmptyResult.Failure)?.error)
            }
            if (phone != null && password != null) {
                return handlePhoneSignIn(phone, password)
            }
            if (refreshToken != null) {
                // currentSession and currentUser will be updated to latest on callRefreshToken using the passed refreshToken
                val response = callRefreshToken(refreshToken)
                response.error?.let { throw Exception(response.error.toString()) }

                return SignInResponse(data = currentSession, user = currentUser, session = currentSession)
            }

            if (provider != null) {
                return handleProviderSignIn(provider, redirectTo, scopes)
            }
            throw Exception("You must provide either an email, phone number or a third-party provider.")
        } catch (e: Exception) {
            return SignInResponse(error = ApiError(e.message!!, -1))
        }
    }

    /**
     * Log in a user given a User supplied OTP received via mobile.
     * @param phone The user's phone number.
     * @param token The user's password.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun verifyOTP(
        phone: String,
        token: String,
        redirectTo: String? = null
    ): SignInResponse {
        try {
            this.removeSession()

            val response = api.verifyMobileOTP(phone, token, redirectTo)

            var session: Session? = null
            var user: User? = null

            when (response) {
                is UserSessionResult.SessionSuccess -> {
                    session = response.data
                    user = session.user as User
                    saveSession(session)
                    notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                }
                is UserSessionResult.UserSuccess -> user = response.data
                is UserSessionResult.Failure -> return SignInResponse(error = response.error)
            }

            return SignInResponse(
                data = (response as UserSessionResult.SessionSuccess).data,
                user = user,
                session = session
            )
        } catch (e: Exception) {
            return SignInResponse(error = ApiError(e.message!!, -1))
        }
    }

    /**
     * Inside a browser context, `user()` will return the user data, if there is a logged in user.
     *
     * For server-side management, you can get a user through `auth.api.getUserByCookie()`
     */
    fun user(): User? {
        return currentUser
    }

    /**
     * Returns the session data, if there is an active session.
     */
    fun session(): Session? {
        return currentSession
    }

    /**
     * Force refreshes the session including the user data in case it was updated in a different session.
     */
    suspend fun refreshSession(): SessionResponse {
        if (currentSession?.accessToken == null) throw Exception("Not logged in.")

        // currentSession and currentUser will be updated to the latest on callRefreshToken
        return callRefreshToken().error?.let { SessionResponse(error = it) }
            ?: SessionResponse(data = currentSession, user = currentUser)
    }

    /**
     * Updates user data, if there is a logged in user.
     */
    suspend fun update(attributes: UserAttributes): UserUpdateResponse {
        val cSession = currentSession ?: return UserUpdateResponse(error = ApiError("No session found.", -1))

        return when (val result = api.updateUser(cSession.accessToken, attributes)) {
            is UserDataResult.Success -> {
                saveSession(cSession.copy(user = result.user))
                notifyAllSubscribers(AuthChangeEvent.USER_UPDATED)
                UserUpdateResponse(data = result.user)
            }
            is UserDataResult.Failure -> UserUpdateResponse(error = result.error)
        }
    }

    /**
     * Sets the session data from refreshToken and returns current Session and Error
     * @param refreshToken a JWT token
     */
    suspend fun setSession(refreshToken: String): SimpleSessionResponse {
        return when (val result = api.refreshAccessToken(refreshToken)) {
            is SessionResult.Success -> {
                saveSession(result.data)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                SimpleSessionResponse(session = result.data)
            }
            is SessionResult.Failure -> SimpleSessionResponse(error = result.error)
        }
    }

    /**
     * Overrides the JWT on the current client. The JWT will then be sent in all subsequent network requests.
     * @param accessToken a jwt access token
     */
    fun setAuth(accessToken: String): Session {
        currentSession = Session(
            providerToken = currentSession?.providerToken,
            accessToken = accessToken,
            expiresIn = currentSession?.expiresIn,
            refreshToken = currentSession?.refreshToken,
            tokenType = "bearer",
            user = null
        )

        return currentSession!!
    }

    /**
     * Gets the session data from a URL string
     * @param storeSession Optionally store the session in the browser
     */
    suspend fun getSessionFromUrl(storeSession: Boolean = false): SessionResponse {
        if (!isBrowser()) throw Exception("No browser detected.")

        val errorDescription = getParameterByName("error_description")
        if (errorDescription != null) throw Exception(errorDescription)

        val providerToken = getParameterByName("provider_token")

        val accessToken = getParameterByName("access_token") ?: throw Exception("No access_token detected.")

        val expiresIn = getParameterByName("expires_in") ?: throw Exception("No expires_in detected.")

        val refreshToken = getParameterByName("refresh_token") ?: throw Exception("No refresh_token detected.")

        val tokenType = getParameterByName("token_type") ?: throw Exception("No token_type detected.")

        return when (val result = api.getUser(accessToken)) {
            is UserDataResult.Success -> {
                val session = Session(
                    providerToken = providerToken,
                    accessToken = accessToken,
                    expiresIn = expiresIn.toLong(),
                    refreshToken = refreshToken,
                    tokenType = tokenType,
                    user = result.user,
                )
                if (storeSession) {
                    saveSession(session)
                    notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                    if (getParameterByName("type") == "recovery") {
                        notifyAllSubscribers(AuthChangeEvent.PASSWORD_RECOVERY)
                    }
                }
                SessionResponse(data = session)
            }
            is UserDataResult.Failure -> SessionResponse(error = result.error)
        }
    }

    /**
     * Inside a browser context, `signOut()` will remove the logged in user from the browser session
     * and log them out - removing all items from localstorage and then trigger a "SIGNED_OUT" event.
     *
     * For server-side management, you can disable sessions by passing a JWT through to `auth.api.signOut(JWT: string)`
     */
    suspend fun signOut(): ApiError? {
        val accessToken = currentSession?.accessToken
        removeSession()
        notifyAllSubscribers(AuthChangeEvent.SIGNED_OUT)

        accessToken?.let {
            val response = api.signOut(accessToken)
            return (response as? EmptyResult.Failure)?.error
        }
        return null
    }

    /**
     * Receive a notification every time an auth event happens.
     * @returns {Subscription} A subscription object which can be used to unsubscribe itself.
     */
    fun onAuthStateChange(callback: (event: AuthChangeEvent, session: Session?) -> Unit): SubscriptionResponse {
        val id: String = uuid()
        val subscription = Subscription(
            id = id,
            callback = callback,
            unsubscribe = { stateChangeEmitters.remove(id) }
        )
        stateChangeEmitters[id] = subscription
        return SubscriptionResponse(data = subscription)
    }

    private suspend fun handleEmailSignIn(
        email: String,
        password: String,
        redirectTo: String? = null
    ): SignInResponse {
        return when (val result: SessionResult = api.signInWithEmail(email, password, redirectTo)) {
            is SessionResult.Success -> {
                if (result.data.user?.confirmedAt != null || result.data.user?.emailConfirmedAt != null) {
                    saveSession(result.data)
                    notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                }

                SignInResponse(data = result.data, user = result.data.user, session = result.data)
            }
            is SessionResult.Failure -> SignInResponse(error = result.error)
        }
    }

    private suspend fun handlePhoneSignIn(phone: String, password: String): SignInResponse {
        return when (val result = api.signInWithPhone(phone, password)) {
            is SessionResult.Success -> {
                if (result.data.user?.phoneConfirmedAt != null) {
                    saveSession(result.data)
                    notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                }

                SignInResponse(data = result.data, user = result.data.user, session = result.data)
            }
            is SessionResult.Failure -> SignInResponse(error = result.error)
        }
    }

    private fun handleProviderSignIn(
        provider: Provider,
        redirectTo: String? = null,
        scopes: String? = null
    ): SignInResponse {
        val url: String = api.getUrlForProvider(provider, redirectTo, scopes)

        // TODO See how to handle provider sign in
        return SignInResponse(provider = provider, url = url)
    }

    /**
     * Attempts to get the session from LocalStorage
     * Note: this should never be async (even for React Native), as we need it to return immediately in the constructor.
     */
    private suspend fun recoverSession() {
        try {
            if (!isBrowser()) return

            // TODO Run blocking or make suspendable here and runBlocking at caller
            val json: String = localStorage?.getItem(STORAGE_KEY) ?: return
            val data = Json.decodeFromString<DataSession>(json)

            val timeNow: Long = timeNow()

            if (data.expiresAt != null && data.expiresAt >= timeNow && data.currentSession?.user != null) {
                saveSession(data.currentSession)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
            }
        } catch (error: Exception) {
            println(error)
        }
    }

    /**
     * Recovers the session from LocalStorage and refreshes
     * Note: this method is async to accommodate for AsyncStorage e.g. in React native.
     */
    private suspend fun recoverAndRefresh() {
        if (!isBrowser()) return

        val json: String = localStorage?.getItem(STORAGE_KEY) ?: return
        val data = Json.decodeFromString<DataSession>(json)
        val timeNow: Long = timeNow()

        if (data.expiresAt == null || data.expiresAt < timeNow) {
            if (autoRefreshToken && data.currentSession?.refreshToken != null) {
                val response = callRefreshToken(data.currentSession.refreshToken)
                response.error?.let {
                    println(response.error)
                    removeSession()
                }
            } else {
                removeSession()
            }
        } else if (data.currentSession?.user == null) {
            println("Current session is missing data.")
            removeSession()
        } else {
            // should be handled on recoverSession method already
            // But we still need the code here to accommodate for AsyncStorage e.g. in React native
            saveSession(data.currentSession)
            notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
        }
    }

    private suspend fun callRefreshToken(refreshToken: String? = currentSession?.refreshToken): RefreshTokenResponse {
        refreshToken?.let { RefreshTokenResponse(error = ApiError("No current session.", -1)) }

        return when (val response = api.refreshAccessToken(refreshToken!!)) {
            is SessionResult.Success -> {
                saveSession(response.data)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                RefreshTokenResponse(data = response.data)
            }
            is SessionResult.Failure -> RefreshTokenResponse(error = response.error)
        }
    }

    private fun notifyAllSubscribers(event: AuthChangeEvent) {
        stateChangeEmitters.forEach { (_, subscription) -> subscription.callback(event, currentSession) }
    }

    /**
     * set currentSession and currentUser
     * process to startAutoRefreshToken if possible
     */
    private fun saveSession(session: Session) {
        currentSession = session
        currentUser = session.user

        val expiresAt = session.expiresAt
        expiresAt?.let {
            val timeNow: Long = timeNow()
            val expiresIn = it - timeNow
            val refreshDurationBeforeExpires: Long = if (expiresIn > 60) 60 else 1 // else was before at 0.5
            startAutoRefreshToken((expiresIn - refreshDurationBeforeExpires) * 1000)
        }

        // Do we need any extra check before persist session
        // accessToken or user ?
        if (persistSession && session.expiresAt != null) {
            persistSession(currentSession!!) // TODO See if using session instead is safer
        }
    }

    private fun persistSession(currentSession: Session) {
        val data = DataSession(currentSession, currentSession.expiresAt)

        if (isBrowser()) {
            CoroutineScope(Dispatchers.Default).launch {
                // TODO See if this works correctly
                localStorage?.setItem(STORAGE_KEY, Json.encodeToString(data))
            }
        }
    }

    private suspend fun removeSession() {
        currentSession = null
        currentUser = null
        refreshTokenTimer?.let { clearTimeout(it) }
        if (isBrowser()) localStorage?.removeItem(STORAGE_KEY)
    }

    /**
     * Clear and re-create refresh token timer
     * @param value time intervals in milliseconds
     */
    private fun startAutoRefreshToken(value: Long) {
        refreshTokenTimer?.let { clearTimeout(it) }
        if (value <= 0 || !autoRefreshToken) return

        refreshTokenTimer = setTimeout({ callRefreshToken() }, value)
        refreshTokenTimer?.unref()
    }
}
