package io.supabase.gotrue

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.supabase.gotrue.domain.MagicLinkType
import io.supabase.gotrue.domain.Provider
import io.supabase.gotrue.http.bodies.*
import io.supabase.gotrue.http.results.*
import io.supabase.gotrue.types.CookieOptions
import io.supabase.gotrue.types.Session
import io.supabase.gotrue.types.User
import io.supabase.gotrue.types.UserAttributes
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiError(
    val message: String,
    val status: Int
)

open class GoTrueApi(
    internal val url: String,
    internal val headers: Headers,
    internal val cookieOptions: CookieOptions, // TODO Use default Cookie Options if any missing
    internal val httpClient: HttpClient = HttpClient()// TODO Convert to HttpClientKtor
    // TODO See if headers need to be passed if already client is passed
) {

    /**
     * Creates a new user using their email address.
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     * @param data Optional user metadata.
     *
     * @returns A logged-in session if the server has "autoconfirm" ON
     * @returns A user if the server has "autoconfirm" OFF
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
            val response: Session = httpClient.post("$url/signup$queryString") {
                headers { this@GoTrueApi.headers }
                body = SignUpEmailBody(email, password, data)
            }

            UserSessionResult.SessionSuccess(response)
        } catch (e: Exception) {
            UserSessionResult.Failure(e as ApiError)
        }
    }

    /**
     * Logs in an existing user using their email address.
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param redirectTo A URL or mobile address to send the user to after they are confirmed.
     */
    suspend fun signInWithEmail(email: String, password: String, redirectTo: String?): SessionResult {
        return try {
            var queryString = "?grant_type=password"
            redirectTo?.let { queryString += "&redirect_to=" + it.encodeURLQueryComponent() }

            val response: Session = httpClient.post("$url/token$queryString") {
                headers { this@GoTrueApi.headers }
                body = SignInEmailBody(email, password)
            }

            SessionResult.Success(response)
        } catch (e: Exception) {
            SessionResult.Failure(e as ApiError)
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
            val response: Session = httpClient.post("$url/signup") {
                headers { this@GoTrueApi.headers }
                body = SignUpPhoneBody(phone, password, data)
            }

            UserSessionResult.SessionSuccess(response) // TODO See if UserSuccess instead
        } catch (e: Exception) {
            UserSessionResult.Failure(e as ApiError)
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
            val response: Session = httpClient.post("$url/token$queryString") {
                headers { this@GoTrueApi.headers }
                body = SignInPhoneBody(phone, password)
            }

            SessionResult.Success(response)
        } catch (e: Exception) {
            SessionResult.Failure(e as ApiError)
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

            httpClient.post<Unit>("$url/magiclink$queryString") {
                headers { this@GoTrueApi.headers }
                body = MagicLinkEmailBody(email)
            }

            EmptyResult.Success()
        } catch (e: Exception) {
            EmptyResult.Failure(e as ApiError)
        }
    }

    /**
     * Sends a mobile OTP via SMS. Will register the account if it doesn't already exist
     * @param phone The user's phone number WITH international prefix
     */
    suspend fun sendMobileOTP(phone: String): MobileOTPResult {
        return try {
            httpClient.post<Unit>("$url/otp") {
                headers { this@GoTrueApi.headers }
                body = MobileOTPBody(phone)
            }
            EmptyResult.Success()
        } catch (e: Exception) {
            EmptyResult.Failure(e as ApiError)
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
            val response: Session = httpClient.post("$url/verify") {
                headers { this@GoTrueApi.headers }
                body = VerifyMobileOTPBody(phone, token, "sms", redirectTo)
            }
            return UserSessionResult.SessionSuccess(response) // TODO See if UserSuccess instead
        } catch (e: Exception) {
            UserSessionResult.Failure(e as ApiError)
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

            val response: User = httpClient.post("$url/invite$queryString") {
                headers { this@GoTrueApi.headers }
                body = EmailInviteBody(email, data)
            }
            UserResult.Success(response)
        } catch (e: Exception) {
            UserResult.Failure(e as ApiError)
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

            httpClient.post<Unit>("$url/recover$queryString") {
                headers { this@GoTrueApi.headers }
                body = { email }
            }
            EmptyResult.Success()
        } catch (e: Exception) {
            EmptyResult.Failure(e as ApiError)
        }
    }

    /**
     * Create a temporary object with all configured headers and
     * adds the Authorization token to be used on request methods
     * @param jwt A valid, logged-in JWT.
     */
    private fun createRequestHeaders(jwt: String): Headers {
        headersOf().apply { headers }
        val h = headersOf().apply {
            headers
            "Authorization" to "Bearer $jwt"
        }
        return h
    }

    /**
     * Removes a logged-in session.
     * @param jwt A valid, logged-in JWT.
     */
    suspend fun signOut(jwt: String): EmptyResult {
        return try {
            httpClient.post<Unit>("$url/logout") {
                headers { createRequestHeaders(jwt) }
                // noResolveJson: true // TODO See how to pass this argument
            }
            EmptyResult.Success()
        } catch (e: Exception) {
            EmptyResult.Failure(e as ApiError)
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

        return "$url/authorize?${urlParams.joinToString("&")}"
    }

    /**
     * Gets the user details.
     * @param jwt A valid, logged-in JWT.
     */
    suspend fun getUser(jwt: String): UserDataResult {
        return try {
            val response: User = httpClient.get("$url/user") {
                headers { createRequestHeaders(jwt) }
            }
            UserDataResult.Success(response, response)
        } catch (e: Exception) {
            UserDataResult.Failure(e as ApiError)
        }
    }

    /**
     * Updates the user data.
     * @param jwt A valid, logged-in JWT.
     * @param attributes The data you want to update.
     */
    suspend fun updateUser(jwt: String, attributes: UserAttributes): UserDataResult {
        return try {
            val response: User = httpClient.put("$url/user") {
                headers { createRequestHeaders(jwt) }
                body = attributes
            }
            UserDataResult.Success(response, response)
        } catch (e: Exception) {
            UserDataResult.Failure(e as ApiError)
        }
    }

    /**
     * Delete a user. Requires a `service_role` key.
     *
     * This function should only be called on a server. Never expose your `service_role` key in the browser.
     *
     * @param uid The user uid you want to remove.
     * @param jwt A valid JWT. Must be a full-access API key (e.g. service_role key).
     */
    suspend fun deleteUser(uid: String, jwt: String): UserDataResult {
        return try {
            val response: User = httpClient.delete("$url/admin/users/$uid") {
                headers { createRequestHeaders(jwt) }
            }
            UserDataResult.Success(response, response)
        } catch (e: Exception) {
            UserDataResult.Failure(e as ApiError)
        }
    }

    /**
     * Generates a new JWT.
     * @param refreshToken A valid refresh token that was returned on login.
     */
    suspend fun refreshAccessToken(refreshToken: String): SessionResult {
        return try {
            val response: Session = httpClient.post("$url/token?grant_type=refresh_token") {
                headers { this@GoTrueApi.headers }
                body = RefreshAccessTokenBody(refreshToken)
            }

            SessionResult.Success(response)
        } catch (e: Exception) {
            SessionResult.Failure(e as ApiError)
        }
    }

    /**
     * Set/delete the auth cookie based on the AuthChangeEvent.
     * Works for Next.js & Express (requires cookie-parser middleware).
     */
    @Deprecated("Use Ktor native authentication procedure.")
    fun setAuthCookie(req: HttpRequest, res: HttpResponsePipeline) {
//        if (req.method != HttpMethod.Post) {
//            // res.setHeader("Allow", "POST") // TODO See how to apply
//            // res.status(405).end("Method Not Allowed") // TODO See how to apply
//        }
//        val body = req.content
//        val event: String = body.getProperty(AttributeKey("event")) ?: throw Exception("Auth event missing!")
//        // TODO event was before body.event, see if this works
//        if (event == "SIGNED_IN") {
//            val session: Session = body.getProperty(AttributeKey("session")) ?: throw Exception("Auth session missing!")
//            // TODO Same as event
//            setCookie(req, res, CookieOptions(
//                name = cookieOptions.name!!,
//                value = session.access_token,
//                domain = cookieOptions.domain,
//                lifetime = cookieOptions.lifetime!!, // key was before maxAge
//                path = cookieOptions.path,
//                sameSite = cookieOptions.sameSite,
//            ))
//        }
//        if (event == "SIGNED_OUT") deleteCookie(req, res, cookieOptions.name!!)
        // res.status(200).json() // TODO See how to apply
    }

    /**
     * Get user by reading the cookie from the request.
     * Works for Next.js & Express (requires cookie-parser middleware).
     */
    @Deprecated("Do not use this function, since it is working with cookies. An alternative solution is under development.")
    suspend fun getUserByCookie(request: HttpRequest): UserDataResult {
//        return try {
//            if (request.cookies == null) throw Exception("Not able to parse cookies! When using Express make sure the cookie-parser middleware is in use!")
//            if (!request.cookies[cookieOptions.name!!]) throw Exception("No cookie found!")
//            val token = request.cookies[cookieOptions.name!!]
//
//            getUser(token)
//        } catch (e: Exception) {
//            UserDataResult.Failure(e as ApiError)
//        }
        TODO("Implement me")
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
            val response: Session = httpClient.post("$url/admin/generate_link") {
                headers { this@GoTrueApi.headers }
                body = MagicLinkGenerationBody(type, email, password, data, redirectTo)
            }
            UserSessionResult.SessionSuccess(response)
        } catch (e: Exception) {
            UserSessionResult.Failure(e as ApiError)
        }
    }
}
