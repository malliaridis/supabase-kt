package io.supabase.gotrue

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.supabase.gotrue.domain.*
import io.supabase.gotrue.http.bodies.*
import io.supabase.gotrue.http.errors.ApiError
import io.supabase.gotrue.http.results.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 *
 * @param url The Base URL of the supabase project including the auth extension of GoTrue (usually `/auth/v1`).
 * @param headers The headers to apply to all requests.
 */
class GoTrueApi(
    private val url: String,
    private val headers: Headers,
    private val cookieOptions: CookieOptions = DEFAULT_COOKIES,
    onAccessToken: ((tokenInfo: Session) -> Unit)? = null,
    onRefreshToken: ((tokenInfo: Session) -> Unit)? = null,
    httpClient: () -> HttpClient
) {

    /**
     * The client to which to apply the auth tokens for authenticated API calls.
     */
    private var tokenClient: HttpClient

    /**
     * A client that is handling unauthorized requests, e.g. for sign-in requests or password recovery.
     */
    private var authClient: HttpClient

    init {
        tokenClient = httpClient().config {
            defaultRequest {
                headers {
                    appendAll(this@GoTrueApi.headers)
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            expectSuccess = false
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            installCustomResponseHandlers()
        }

        authClient = tokenClient
    }

    /**
     * Create a temporary object with all configured headers and adds the Authorization token to be used on request
     * methods.
     * @param jwt A valid, logged-in JWT.
     */
    private fun createRequestHeaders(jwt: String): Headers = HeadersBuilder().apply {
        appendAll(headers)
        append("Authorization", "Bearer $jwt")
    }.build()

    private fun cookieName(): String = cookieOptions.name ?: ""

    /**
     * Generates the relevant login URL for a third-party provider.
     * @param provider One of the providers supported by GoTrue.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param scopes A space-separated list of scopes granted to the OAuth application.
     */
    fun getUrlForProvider(
        provider: Provider,
        redirectTo: String?,
        scopes: String?,
        queryParams: Map<String, String>?
    ): Url = URLBuilder("$url/authorize")
        .apply {
            parameters.append("provider", provider.toString().encodeURLQueryComponent())
            redirectTo?.let { parameters.append("redirect_to", redirectTo.encodeURLQueryComponent()) }
            scopes?.let { parameters.append("scopes", scopes.encodeURLQueryComponent()) }

            queryParams?.forEach { _ -> parameters::append }
        }
        .build()

    /**
     * Creates a new user using their email address.
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param data Optional user metadata.
     *
     * @returns A logged-in session if the server has `autoconfirm` ON
     * @returns A user if the server has `autoconfirm` OFF
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        redirectTo: String?,
        data: JsonObject?,
        captchaToken: String?
    ): UserSessionResult = try {
        val session: Session = tokenClient.post("$url/signup") {
            redirectTo?.let { parameter("redirect_to", it.encodeURLQueryComponent()) }
            setBody(SignUpEmailBody(email, password, data, captchaToken))
        }.body()

        configureAuthClient(session)

        UserSessionResult.SessionSuccess(session)
    } catch (error: ApiError) {
        UserSessionResult.Failure(error)
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun signInWithEmail(email: String, password: String, redirectTo: String? = null): SessionResult = try {

        val session: Session = tokenClient.post("$url/token") {
            parameter("grant_type", "password")
            redirectTo?.let { parameter("redirect_to", redirectTo.encodeURLQueryComponent()) }
            setBody(SignInEmailBody(email, password))
        }.body()

        configureAuthClient(session)

        SessionResult.Success(session)
    } catch (error: ApiError) {
        SessionResult.Failure(error)
    }

    /**
     * Signs up a new user using their phone number and a password.
     * @param phone The phone number of the user.
     * @param password The password of the user.
     * @param data Optional user metadata.
     */
    suspend fun signUpWithPhone(
        phone: String,
        password: String,
        data: JsonObject?,
        captchaToken: String?
    ): UserSessionResult = try {

        val session: Session = tokenClient.post("$url/signup") {
            setBody(SignUpPhoneBody(phone, password, data, captchaToken))
        }.body()

        configureAuthClient(session)

        UserSessionResult.SessionSuccess(session) // TODO See if UserSuccess instead
    } catch (error: ApiError) {
        UserSessionResult.Failure(error)
    }

    /**
     * Logs in an existing user using their phone number and password.
     * @param phone The phone number of the user.
     * @param password The password of the user.
     */
    suspend fun signInWithPhone(phone: String, password: String): SessionResult = try {
        val session: Session = tokenClient.post("$url/token") {
            parameter("grant_type", "password")
            setBody(SignInPhoneBody(phone, password))
        }.body()

        configureAuthClient(session)

        SessionResult.Success(session)
    } catch (error: ApiError) {
        SessionResult.Failure(error)
    }

    /**
     * Logs in an OpenID Connect user using their [OpenIDConnectCredentials.idToken].
     * @param credentials The OpenID Connect credentials
     */
    suspend fun signInWithOpenIDConnect(credentials: OpenIDConnectCredentials): SessionResult = try {
        val session: Session = tokenClient.post("$url/token") {
            parameter("grant_type", "id_token")
            setBody(credentials)
        }.body()

        configureAuthClient(session)

        SessionResult.Success(session)
    } catch (error: ApiError) {
        SessionResult.Failure(error)
    }

    /**
     *  Configures the authenticated client with the provided session. Used after sign up or sign in.
     *  @param session The sessions to use.
     */
    private fun configureAuthClient(session: Session) {
        authClient = authClient.config {
            defaultRequest {
                headers {
                    appendAll(this@GoTrueApi.headers)
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            expectSuccess = false
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            installCustomResponseHandlers()
            installAuth(session)
        }
    }

    /**
     * Sends a magic login link to an email address.
     *
     * @param email The email address of the user.
     * @param shouldCreateUser A boolean flag to indicate whether to automatically create a user on magiclink / otp
     * sign-ins if the user doesn't exist. Defaults to true.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun sendMagicLinkEmail(
        email: String,
        shouldCreateUser: Boolean = true,
        redirectTo: String?,
        captchaToken: String?
    ): MagicLinkResult = try {
        tokenClient.post("$url/magiclink") {
            redirectTo?.let { parameter("redirect_to", it.encodeURLQueryComponent()) }
            setBody(MagicLinkEmailBody(email, shouldCreateUser, captchaToken))
        }

        // TODO Validate response before returning empty response
        EmptyResult.Success()
    } catch (error: ApiError) {
        EmptyResult.Failure(error)
    }

    /**
     * Sends a mobile OTP via SMS. Will register the account if it doesn't already exist.
     *
     * @param phone The user's phone number WITH international prefix.
     * @param shouldCreateUser A boolean flag to indicate whether to automatically create a user on magiclink / otp
     * sign-ins if the user doesn't exist. Defaults to true.
     */
    suspend fun sendMobileOTP(
        phone: String,
        shouldCreateUser: Boolean = true,
        captchaToken: String?
    ): MobileOTPResult = try {
        tokenClient.post("$url/otp") {
            setBody(MobileOTPBody(phone, shouldCreateUser, captchaToken))
        }
        EmptyResult.Success()
    } catch (error: ApiError) {
        EmptyResult.Failure(error)
    }

    /**
     * Removes a logged-in session.
     * @param jwt A valid, logged-in JWT. If not provided, [authClient] is used for signing out.
     */
    suspend fun signOut(jwt: String? = null): EmptyResult {
        return try {
            jwt?.let {
                tokenClient.post("$url/logout") {
                    header("Authorization", "Bearer $it")
                }
            } ?: run { authClient.post("$url/logout") }

            authClient = tokenClient

            EmptyResult.Success()
        } catch (error: ApiError) {
            EmptyResult.Failure(error)
        }
    }

    /**
     * Send User supplied Email / Mobile OTP to be verified
     * @param params The OTP parameters.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun verifyOTP(
        params: VerifyOTPParams,
        redirectTo: String? = null
    ): UserSessionResult {
        return try {
            authClient.post("$url/verify") {
                headers { this@GoTrueApi.headers }
                TODO("Write OTPRequestBody")
                // { email, phone, token, type, redirect_to: options.redirectTo },
            }.body()

            // Removed expiresAt logic since result does not contain expiresIn value
        } catch (e: Exception) {
            UserSessionResult.Failure(error = ApiError(e.message!!, -1))
        }
    }

    /**
     * Sends an invitation link to an email address.
     * @param email The email address of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param data Optional user metadata
     */
    suspend fun inviteUserByEmail(email: String, redirectTo: String?, data: JsonElement?): UserResult {
        return try {
            var queryString = ""
            redirectTo?.let { queryString += "?redirect_to=" + redirectTo.encodeURLQueryComponent() }

            val response: User = tokenClient.post("$url/invite$queryString") {
                setBody(EmailInviteBody(email, data))
            }.body()

            UserResult.Success(response)
        } catch (error: ApiError) {
            UserResult.Failure(error)
        }
    }

    /**
     * Sends a reset request to an email address.
     * @param email The email address of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun resetPasswordForEmail(email: String, redirectTo: String?): ResetPasswordResult {
        return try {
            var queryString = ""
            redirectTo?.let { queryString += "?redirect_to=" + redirectTo.encodeURLQueryComponent() }

            tokenClient.post("$url/recover$queryString") {
                setBody { email }
            }
            EmptyResult.Success()
        } catch (error: ApiError) {
            EmptyResult.Failure(error)
        }
    }

    /**
     * Gets the current user details.
     *
     * This method is called by the GoTrueClient `update` where
     * the jwt is set to this.currentSession.access_token
     * and therefore, acts like getting the currently authenticated user
     *
     * @param jwt A valid, logged-in JWT. Typically, the access_token for the currentSession
     */
    suspend fun getUser(jwt: String): UserDataResult {
        return try {
            val response: User = authClient.get("$url/user") {
                headers { createRequestHeaders(jwt) }
            }.body()
            UserDataResult.Success(response, response)
        } catch (error: ApiError) {
            UserDataResult.Failure(error)
        }
    }

    /**
     * Updates the user data.
     * @param attributes The data you want to update.
     */
    suspend fun updateUser(attributes: UserAttributes): UserDataResult {
        return try {
            val response: User = authClient.put("$url/user") {
                setBody(attributes)
            }.body()

            UserDataResult.Success(response, response)
        } catch (error: ApiError) {
            UserDataResult.Failure(error)
        }
    }

    /**
     * Delete a user. Requires a `service_role` key.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     *
     * @param uid The user uid you want to remove.
     */
    suspend fun deleteUser(uid: String): UserDataResult {
        return try {
            val response: User = authClient.delete("$url/admin/users/$uid").body()
            UserDataResult.Success(response, response)
        } catch (error: ApiError) {
            UserDataResult.Failure(error)
        }
    }

    /**
     * Generates a new JWT.
     * @param refreshToken A valid refresh token that was returned on login.
     */
    suspend fun refreshAccessToken(refreshToken: String): SessionResult {
        return try {
            val response: Session = tokenClient.post("$url/token?grant_type=refresh_token") {
                setBody(RefreshAccessTokenBody(refreshToken))
            }.body()

            SessionResult.Success(response)
        } catch (error: ApiError) {
            SessionResult.Failure(error)
        }
    }

    /**
     * Generates links to be sent via email or other.
     * @param type The link type ("signup" or "magiclink" or "recovery" or "invite").
     * @param email The user's email.
     * @param password User password. For signup only.
     * @param data Optional user metfadata. For signup only.
     * @param redirectTo The link type ("signup" or "magiclink" or "recovery" or "invite").
     */
    suspend fun generateLink(
        type: MagicLinkType,
        email: String,
        password: String?,
        data: JsonElement?,
        redirectTo: String?
    ): UserSessionResult {
        return try {
            val response: Session = tokenClient.post("$url/admin/generate_link") {
                setBody(MagicLinkGenerationBody(type, email, password, data, redirectTo))
            }.body()
            UserSessionResult.SessionSuccess(response)
        } catch (error: ApiError) {
            UserSessionResult.Failure(error)
        }
    }

    suspend fun getSettings(): Settings = tokenClient.get("$url/settings").body()

    fun auth(): HttpClient = authClient

    /**
     *
     * @param type Can be signup, recover or invite
     * @param token The token provided with the link on creation
     * @param password The password to set in case of a signup. This is required if no password was set on [signUpWithEmail].
     */
    suspend fun verify(type: MagicLinkType, token: String, password: String?): Session {
        TODO("Not implemented yet")
        // Can be GET or POST request
        // GET request receives a response as:
        // SITE_URL/#access_token=jwt-token-representing-the-user&token_type=bearer&expires_in=3600&refresh_token=a-refresh-token&type=invite
        // GET response with type invite or recovery should redirect user to password set
        // GET response with type signup can show welcome page
    }

    /**
     * Requests a one-time-password. Can be sent either on [email] or [phone]. Will deliver a magiclink or sms otp to
     * the user depending on whether the request contains an "email" or "phone" key.
     *
     * @param email The email to send the password to
     * @param phone The phone number to send the password to
     */
    suspend fun sendOTP(email: String?, phone: String?) {
        TODO("Not implemented yet")
    }

    /**
     * Password recovery. Will deliver a password recovery mail to the user based on email address. By default, recovery
     * links can only be sent once every 60 seconds.
     *
     * @param email The email of the user to send a password recovery mail.
     */
    suspend fun recover(email: String) {
        TODO("Not implemented yet")
    }

    /**
     * Custom response handler that parses the client exceptions to [ApiError]s.
     */
    private fun HttpClientConfig<*>.installCustomResponseHandlers() {
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess())
                    throw ApiError(response.bodyAsText(), response.status.value)
            }
        }
    }

    /**
     * Extension function that authenticates the [HttpClient] with email and password.
     */
    private fun HttpClientConfig<*>.installAuth(session: Session) {
        Auth {
            var refreshTokenInfo: Session

            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken!!
                    )
                }

                refreshTokens {
                    refreshTokenInfo = tokenClient.post("$url/token") {
                        setBody(RefreshAccessTokenBody(this@refreshTokens.oldTokens!!.refreshToken))
                    }.body()
                    BearerTokens(
                        accessToken = refreshTokenInfo.accessToken,
                        refreshToken = session.refreshToken!! // TODO See if the new refresh token is from refreshTokenInfo
                    )
                }
            }
        }
    }

}
