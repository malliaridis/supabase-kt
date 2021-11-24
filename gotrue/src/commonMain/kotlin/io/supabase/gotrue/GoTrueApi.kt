package io.supabase.gotrue

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.supabase.gotrue.domain.*
import io.supabase.gotrue.http.bodies.*
import io.supabase.gotrue.http.errors.ApiError
import io.supabase.gotrue.http.results.*
import io.supabase.gotrue.types.UserAttributes
import kotlinx.serialization.json.JsonElement

private const val goTrue = "/auth/v1"

/**
 *
 * @param url The Base URL of the supabase project.
 * @param headers The headers to apply to all requests.
 */
class GoTrueApi(
    private val url: String,
    private val headers: Headers,
    onAccessToken: ((tokenInfo: Session) -> Unit)? = null,
    onRefreshToken: ((tokenInfo: Session) -> Unit)? = null
) {

    /**
     * The client to which to apply the auth tokens for authenticated API calls.
     */
    private var tokenClient: HttpClient = HttpClient()

    /**
     * A client that is handling unauthorized requests, e.g. for sign-in requests or password recovery.
     */
    private var authClient: HttpClient = tokenClient // TODO See if the tokenClient can be used

    init {
        tokenClient = tokenClient.config {
            defaultRequest {
                headers {
                    this@GoTrueApi.headers.forEach { key, value -> appendAll(key, value) }
                    append(HttpHeaders.ContentType, "application/json")
                }
            }
            expectSuccess = false
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
            installCustomResponseHandlers()
        }
    }

    /**
     * Register a new user with an email and password.
     *
     * @param email The email to register
     * @param password The password to set for the new account. If not set it needs to be provided on [verify]
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        redirectTo: String?,
        data: JsonElement?
    ): UserSessionResult {
        return try {
            var queryString = ""
            redirectTo?.let { queryString = "?redirect_to=" + it.encodeURLQueryComponent() }
            val result: Session = tokenClient.post("$url$goTrue/signup$queryString") {
                body = SignUpEmailBody(email, password, data)
            }

            return UserSessionResult.SessionSuccess(result)
        } catch (error: ApiError) {
            UserSessionResult.Failure(error)
        }
    }

    /**
     * Logs in an existing user using their email address.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun signInWithEmail(email: String, password: String, redirectTo: String? = null): SessionResult {
        return try {
            val session: Session = tokenClient.post("$url$goTrue/token") {
                parameter("grant_type", "password")
                body = SignInEmailBody(email, password)
            }

            authClient = authClient.config {
                defaultRequest {
                    headers {
                        this@GoTrueApi.headers.forEach { key, value -> appendAll(key, value) }
                        append(HttpHeaders.ContentType, "application/json")
                    }
                }
                expectSuccess = false
                install(JsonFeature) {
                    serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
                }
                installCustomResponseHandlers()
                installAuth(session)
            }

            SessionResult.Success(session)
        } catch (error: ApiError) {
            SessionResult.Failure(error)
        }
    }

    /**
     * Signs up a new user using their phone number and a password.
     * @param phone The phone number of the user.
     * @param password The password of the user.
     * @param data Optional user metadata.
     */
    suspend fun signUpWithPhone(phone: String, password: String, data: JsonElement?): UserSessionResult {
        return try {
            val response: Session = tokenClient.post("$url$goTrue/signup") {
                body = SignUpPhoneBody(phone, password, data)
            }

            UserSessionResult.SessionSuccess(response) // TODO See if UserSuccess instead
        } catch (error: ApiError) {
            UserSessionResult.Failure(error)
        }
    }

    /**
     * Logs in an existing user using their phone number and password.
     * @param phone The phone number of the user.
     * @param password The password of the user.
     */
    suspend fun signInWithPhone(phone: String, password: String): SessionResult {
        return try {
            val queryString = "?grant_type=password"
            val response: Session = tokenClient.post("$url$goTrue/token$queryString") {
                body = SignInPhoneBody(phone, password)
            }

            SessionResult.Success(response)
        } catch (error: ApiError) {
            SessionResult.Failure(error)
        }
    }

    /**
     * Sends a magic login link to an email address.
     * @param email The email address of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun sendMagicLinkEmail(email: String, redirectTo: String?): MagicLinkResult {
        return try {
            var queryString = ""
            redirectTo?.let { queryString += "?redirect_to=" + it.encodeURLQueryComponent() } // TODO See if encodeUrlParameter is correct

            tokenClient.post<Unit>("$url$goTrue/magiclink$queryString") {
                body = MagicLinkEmailBody(email)
            }

            EmptyResult.Success()
        } catch (error: ApiError) {
            EmptyResult.Failure(error)
        }
    }

    /**
     * Sends a mobile OTP via SMS. Will register the account if it doesn't already exist
     * @param phone The user's phone number WITH international prefix
     */
    suspend fun sendMobileOTP(phone: String): MobileOTPResult {
        return try {
            tokenClient.post<Unit>("$url$goTrue/otp") {
                body = MobileOTPBody(phone)
            }
            EmptyResult.Success()
        } catch (error: ApiError) {
            EmptyResult.Failure(error)
        }
    }

    /**
     * Send User supplied Mobile OTP to be verified
     * @param phone The user's phone number WITH international prefix
     * @param token token that user was sent to their mobile phone
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun verifyMobileOTP(phone: String, token: String, redirectTo: String?): UserSessionResult {
        return try {
            val response: Session = tokenClient.post("$url$goTrue/verify") {
                body = VerifyMobileOTPBody(phone, token, "sms", redirectTo)
            }

            return UserSessionResult.SessionSuccess(response) // TODO See if UserSuccess instead
        } catch (error: ApiError) {
            UserSessionResult.Failure(error)
        }
    }

    /**
     * Sends an invite link to an email address.
     * @param email The email address of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param data Optional user metadata
     */
    suspend fun inviteUserByEmail(email: String, redirectTo: String?, data: JsonElement?): UserResult {
        return try {
            var queryString = ""
            redirectTo?.let { queryString += "?redirect_to=" + redirectTo.encodeURLQueryComponent() }

            val response: UserInfo = tokenClient.post("$url$goTrue/invite$queryString") {
                body = EmailInviteBody(email, data)
            }

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

            tokenClient.post<Unit>("$url$goTrue/recover$queryString") {
                body = { email }
            }
            EmptyResult.Success()
        } catch (error: ApiError) {
            EmptyResult.Failure(error)
        }
    }

    /**
     * Removes a logged-in session.
     */
    suspend fun signOut(): EmptyResult {
        return try {
            authClient.post<Unit>("$url$goTrue/logout")
            authClient = tokenClient

            EmptyResult.Success()
        } catch (error: ApiError) {
            EmptyResult.Failure(error)
        }
    }

    /**
     * Generates the relevant login URL for a third-party provider.
     * @param provider One of the providers supported by GoTrue.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param scopes A space-separated list of scopes granted to the OAuth application.
     */
    fun getUrlForProvider(provider: Provider, redirectTo: String?, scopes: String?): String {
        val urlParams: MutableList<String> = mutableListOf("provider=${provider.toString().encodeURLQueryComponent()}")

        redirectTo?.let { urlParams.add("redirect_to=${redirectTo.encodeURLQueryComponent()}") }
        scopes?.let { urlParams.add("scopes=${scopes.encodeURLQueryComponent()}") }

        return "$url$goTrue/authorize?${urlParams.joinToString("&")}"
    }

    /**
     * Gets the user details.
     */
    suspend fun getUser(): UserDataResult {
        return try {
            val response: UserInfo = authClient.get("$url$goTrue/user")
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
            val response: UserInfo = authClient.put("$url$goTrue/user") {
                body = attributes
            }

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
            val response: UserInfo = authClient.delete("$url$goTrue/admin/users/$uid")
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
                body = RefreshAccessTokenBody(refreshToken)
            }

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
            val response: Session = tokenClient.post("$url$goTrue/admin/generate_link") {
                body = MagicLinkGenerationBody(type, email, password, data, redirectTo)
            }
            UserSessionResult.SessionSuccess(response)
        } catch (error: ApiError) {
            UserSessionResult.Failure(error)
        }
    }

    suspend fun getSettings(): Settings {
        return tokenClient.get("$url$goTrue/settings")
    }

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
                    throw ApiError(response.readText(), response.status.value)
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

                refreshTokens { unauthorizedResponse: HttpResponse ->
                    // TODO See if tokenClient needs to be authClient instead
                    refreshTokenInfo = tokenClient.post("$url$goTrue/token") {
                        body = RefreshAccessTokenBody(session.refreshToken!!)
                    }
                    BearerTokens(
                        accessToken = refreshTokenInfo.accessToken,
                        refreshToken = session.refreshToken!! // TODO See if the new refresh token is from refreshTokenInfo
                    )
                }
            }
        }
    }

}
