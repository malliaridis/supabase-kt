package io.supabase.gotrue

import io.ktor.client.*
import io.supabase.gotrue.helper.*
import io.supabase.gotrue.http.*
import io.supabase.gotrue.types.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Create a new client for use in the browser.
 */
open class GoTrueClient(
    private val url: String = "http://localhost:9999",
    private val headers: Map<String, String> = mapOf("X-Client-Info" to "gotrue-js/0.0.0"),
    private val detectSessionInUrl: Boolean = true,
    internal val autoRefreshToken: Boolean = true,
    internal val persistSession: Boolean = true,
    internal var localStorage: SupportedStorage? = TODO(),
    private val cookieOptions: CookieOptions = DEFAULT_COOKIES,
    private val goTrueHttpClient: GoTrueHttpClient = GoTrueHttpClientKtor(
        url = url,
        globalHeaders = headers,
        httpClient = { HttpClient() }
    )
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
        cookieOptions = cookieOptions,
        httpClient = goTrueHttpClient,
    )

    internal var stateChangeEmitters: MutableMap<String, Subscription> = mutableMapOf()
    internal var refreshTokenTimer: Timer? = null

    init {
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
        data: Any? = null
    ): SignUpResponse {
        try {
            removeSession()

            val response: SignUpResponse = // TODO See if SignUpResponse is correct here
                if (phone != null && password != null) api.signUpWithPhone(phone, password, data)
                else api.signUpWithEmail(email!!, password!!, redirectTo, data)

            response.error?.let { throw Exception(it) }
            if (response.data == null) throw Exception("An error occurred on sign up.")

            var session: Session? = null
            var user: User? = null

            if ((data as? Session)?.access_token != null) {
                session = data
                user = session.user as User
                saveSession(session)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
            }

            if ((data as? User)?.id != null) {
                user = data
            }

            return SignUpResponse(data = data, user = user, session = session)
        } catch (e: Exception) {
            return SignUpResponse(data = null, user = null, session = null, error = e as ApiError)
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
                return SignInResponse(error = response.error)
            }
            if (email != null && password != null) {
                return handleEmailSignIn(email, password, redirectTo)
            }
            if (phone != null && password == null) {
                val response = api.sendMobileOTP(phone)
                return SignInResponse(error = response.error)
            }
            if (phone != null && password != null) {
                return handlePhoneSignIn(phone, password)
            }
            if (refreshToken != null) {
                // currentSession and currentUser will be updated to latest on callRefreshToken using the passed refreshToken
                val response = callRefreshToken(refreshToken)
                response.error?.let { throw Exception(it) }

                return SignInResponse(data = currentSession, user = currentUser, session = currentSession)
            }

            if (provider != null) {
                return handleProviderSignIn(provider, redirectTo, scopes)
            }
            throw Exception("You must provide either an email, phone number or a third-party provider.")
        } catch (e: Exception) {
            return SignInResponse(error = e as ApiError)
        }
    }

    /**
     * Log in a user given a User supplied OTP received via mobile.
     * @param phone The user's phone number.
     * @param token The user's password.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun verifyOTP(
        phone: String?,
        token: String?,
        redirectTo: String? = null
    ): SignInResponse {
        try {
            this.removeSession()

            val response = api.verifyMobileOTP(phone, token, redirectTo)

            with(response) {
                if (error != null) throw error

                if (data == null) throw Exception("An error occurred on token verification.")

                var session: Session? = null
                var user: User? = null

                if ((data as? Session)?.access_token != null) {
                    session = data
                    user = session.user as User
                    saveSession(session)
                    notifyAllSubscribers(io.supabase.gotrue.types.AuthChangeEvent.SIGNED_IN)
                }

                if ((data as? User)?.id != null) user = data

                return SignInResponse(data = data, user = user, session = session)
            }
        } catch (e: Exception) {
            return SignInResponse(error = e as ApiError)
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
        try {
            if (currentSession?.access_token == null) throw Exception("Not logged in.")

            // currentSession and currentUser will be updated to latest on callRefreshToken
            val response = callRefreshToken()
            response.error?.let { throw Exception(it) }

            return SessionResponse(data = currentSession, user = currentUser)
        } catch (e: Exception) {
            return SessionResponse(error = e as ApiError)
        }
    }

    /**
     * Updates user data, if there is a logged in user.
     */
    suspend fun update(attributes: UserAttributes): UserUpdateResponse {
        try {
            val cSession = currentSession
            if (cSession?.access_token == null) throw Exception("Not logged in.")

            val response = api.updateUser(cSession.access_token, attributes)

            response.error?.let { throw Exception(it) }
            if (response.user == null) throw Exception("Invalid user data.")

            val session = cSession.copy(user = response.user)

            saveSession(session)
            notifyAllSubscribers(AuthChangeEvent.USER_UPDATED)

            return UserUpdateResponse(data = response.user)
        } catch (e: Exception) {
            return UserUpdateResponse(error = e as ApiError)
        }
    }

    /**
     * Sets the session data from refresh_token and returns current Session and Error
     * @param refresh_token a JWT token
     */
    suspend fun setSession(refresh_token: String?): SimpleSessionResponse {
        try {
            if (refresh_token == null) throw Exception("No current session.")

            val response = api.refreshAccessToken(refresh_token)
            if (response.error != null) return SimpleSessionResponse(error = response.error)

            this.saveSession(response.data!!)
            this.notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
            return SimpleSessionResponse(session = response.data)
        } catch (e: Exception) {
            return SimpleSessionResponse(error = e as ApiError)
        }
    }

    /**
     * Overrides the JWT on the current client. The JWT will then be sent in all subsequent network requests.
     * @param access_token a jwt access token
     */
    fun setAuth(access_token: String): Session {
        currentSession = Session(
            provider_token = currentSession?.provider_token,
            access_token = access_token,
            expires_in = currentSession?.expires_in,
            expires_at = currentSession?.expires_at,
            refresh_token = currentSession?.refresh_token,
            token_type = "bearer",
            user = null
        )

        return currentSession!!
    }

    /**
     * Gets the session data from a URL string
     * @param options.storeSession Optionally store the session in the browser
     */
    suspend fun getSessionFromUrl(storeSession: Boolean?): SessionResponse {
        try {
            if (!isBrowser()) throw Exception("No browser detected.")

            val errorDescription = getParameterByName("error_description")
            if (errorDescription != null) throw Exception(errorDescription)

            val providerToken = getParameterByName("provider_token")

            val accessToken = getParameterByName("access_token") ?: throw Exception("No access_token detected.")

            val expiresIn = getParameterByName("expires_in") ?: throw Exception("No expires_in detected.")

            val refreshToken = getParameterByName("refresh_token") ?: throw Exception("No refresh_token detected.")

            val tokenType = getParameterByName("token_type") ?: throw Exception("No token_type detected.")

            val timeNow = timeNow()
            val expiresAt = timeNow + expiresIn.toLong()

            val response = api.getUser(accessToken)
            response.error?.let { throw Exception(it) }

            val session = Session(
                provider_token = providerToken,
                access_token = accessToken,
                expires_in = expiresIn.toLong(),
                expires_at = expiresAt,
                refresh_token = refreshToken,
                token_type = tokenType,
                user = response.user!!,
            )
            if (options.storeSession) {
                saveSession(session)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
                if (getParameterByName("type") == "recovery") {
                    notifyAllSubscribers(AuthChangeEvent.PASSWORD_RECOVERY)
                }
            }
            // Remove tokens from URL
            // window.location.hash = ""

            return SessionResponse(data = session)
        } catch (e: Exception) {
            return SessionResponse(error = e as ApiError)
        }
    }

    /**
     * Inside a browser context, `signOut()` will remove the logged in user from the browser session
     * and log them out - removing all items from localstorage and then trigger a "SIGNED_OUT" event.
     *
     * For server-side management, you can disable sessions by passing a JWT through to `auth.api.signOut(JWT: string)`
     */
    suspend fun signOut(): ApiError? {
        val accessToken = currentSession?.access_token
        removeSession()
        notifyAllSubscribers(AuthChangeEvent.SIGNED_OUT)

        accessToken?.let {
            val response = api.signOut(accessToken)
            return response.error
        }
        return null
    }

    /**
     * Receive a notification every time an auth event happens.
     * @returns {Subscription} A subscription object which can be used to unsubscribe itself.
     */
    fun onAuthStateChange(callback: (event: AuthChangeEvent, session: Session?) -> Unit): SubscriptionResponse {
        try {
            val id: String = uuid()
            val subscription = Subscription(
                id = id,
                callback = callback,
                unsubscribe = { stateChangeEmitters.remove(id) }
            )
            stateChangeEmitters[id] = subscription
            return SubscriptionResponse(data = subscription)
        } catch (e: Exception) {
            return SubscriptionResponse(error = e as ApiError)
        }
    }

    private suspend fun handleEmailSignIn(
        email: String,
        password: String,
        redirectTo: String? = null
    ): SignInResponse {
        try {
            val response: SignInResponse = api.signInWithEmail(email, password, redirectTo)
            if (response.error != null || response.data == null) return SignInResponse(error = response.error)

            if (response.data.user?.confirmed_at != null || response.data.user?.email_confirmed_at != null) {
                saveSession(response.data)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
            }

            return SignInResponse(data = response.data, user = response.data.user, session = response.data)
        } catch (e: Exception) {
            return SignInResponse(error = e as ApiError)
        }
    }

    private suspend fun handlePhoneSignIn(phone: String, password: String): SignInResponse {
        try {
            val response = api.signInWithPhone(phone, password)

            if (response.error != null || response.data == null)
                return SignInResponse(error = response.error)

            if (response.data?.user?.phone_confirmed_at) {
                saveSession(response.data)
                notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)
            }

            return SignInResponse(data = response.data, user = response.data.user, session = response.data)
        } catch (e: Exception) {
            return SignInResponse(error = e as ApiError)
        }
    }

    private fun handleProviderSignIn(
        provider: Provider,
        redirectTo: String? = null,
        scopes: String? = null
    ): SignInResponse {
        val url: String? = api.getUrlForProvider(provider, redirectTo, scopes)

        try {
            // try to open on the browser
            // if (isBrowser()) window.location.href = url
            // TODO See how to handle provider sign in

            return SignInResponse(provider = provider, url = url)
        } catch (e: Exception) {
            // fallback to returning the URL
            url?.let {
                return SignInResponse(provider = provider, url = url)
            } ?: run {
                return SignInResponse(error = e as ApiError)
            }
        }
    }

    /**
     * Attempts to get the session from LocalStorage
     * Note: this should never be async (even for React Native), as we need it to return immediately in the constructor.
     */
    private fun recoverSession() {
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
        try {
            if (!isBrowser()) return

            val json: String = localStorage.getItem(STORAGE_KEY) ?: return

            val data = Json.decodeFromString<DataSession>(json)

            val timeNow: Long = timeNow()

            if (data.expiresAt == null || data.expiresAt < timeNow) {
                if (autoRefreshToken && data.currentSession?.refresh_token != null) {
                    val response = callRefreshToken(data.currentSession.refresh_token)
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
        } catch (err: Exception) {
            println(err)
            return
        }
    }

    private suspend fun callRefreshToken(refresh_token: String? = currentSession?.refresh_token): RefreshTokenResponse {
        return try {
            refresh_token?.let { throw Exception("No current session.") }

            val response = api.refreshAccessToken(refresh_token)
            response.error?.let { throw Exception(it) }

            if (response.data == null) throw Exception("Invalid session data.")

            saveSession(response.data)
            notifyAllSubscribers(AuthChangeEvent.SIGNED_IN)

            RefreshTokenResponse(data = response.data)
        } catch (e: Exception) {
            RefreshTokenResponse(error = e as ApiError)
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

        val expiresAt = session.expires_at
        expiresAt?.let {
            val timeNow: Long = timeNow()
            val expiresIn = it - timeNow
            val refreshDurationBeforeExpires: Long = if (expiresIn > 60) 60 else 1 // else was before at 0.5
            startAutoRefreshToken((expiresIn - refreshDurationBeforeExpires) * 1000)
        }

        // Do we need any extra check before persist session
        // access_token or user ?
        if (persistSession && session.expires_at != null) {
            persistSession(currentSession!!) // TODO See if using session instead is safer
        }
    }

    private fun persistSession(currentSession: Session) {
        val data = DataSession(currentSession, currentSession.expires_at)
        if (isBrowser()) localStorage.setItem(STORAGE_KEY, Json.encodeToString(data))
    }

    private suspend fun removeSession() {
        currentSession = null
        currentUser = null
        refreshTokenTimer?.let { clearTimeout(it) }
        if (isBrowser()) localStorage.removeItem(STORAGE_KEY)
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
