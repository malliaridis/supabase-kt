package io.supabase.gotrue.ktor

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
import io.supabase.gotrue.http.bodies.RefreshAccessTokenBody
import io.supabase.gotrue.http.bodies.SignInEmailBody
import kotlinx.serialization.json.JsonElement

private const val goTrue = "/auth/v1"

/**
 *
 * @param url The Base URL of the supabase project.
 * @param apiKey The API key of the Supabase project.
 * @param authClient The client to which to apply the auth tokens for authenticated API calls.
 * @param tokenClient A client that is handling unauthorized requests, e.g. for sign-in requests or password recovery.
 */
class GoTrueKtorClient(
    private val url: String,
    private val apiKey: String,
    private var authClient: HttpClient = HttpClient(),
    private var tokenClient: HttpClient = HttpClient()
) {

    init {
        tokenClient = tokenClient.config {
            defaultRequest {
                header("apikey", apiKey)
                header(HttpHeaders.ContentType, "application/json")
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        }
    }

    /**
     * Extension function that authenticates the [HttpClient] with email and password.
     */
    private fun HttpClientConfig<*>.installAuth(email: String, password: String) {
        install(Auth) {
            lateinit var tokenInfo: TokenInfo
            var refreshTokenInfo: TokenInfo

            bearer {
                loadTokens {
                    // TODO See if there is a session stored in localStorage
                    tokenInfo = tokenClient.post("$url$goTrue/token") {
                        parameter("grant_type", "password")
                        body = SignInEmailBody(email, password)
                    }
                    BearerTokens(
                        accessToken = tokenInfo.accessToken,
                        refreshToken = tokenInfo.refreshToken!!
                    )
                    // TODO Save session in localStorage here
                }

                refreshTokens { unauthorizedResponse: HttpResponse ->
                    refreshTokenInfo = tokenClient.post("$url$goTrue/token") {
                        body = RefreshAccessTokenBody(tokenInfo.refreshToken!!)
                    }
                    BearerTokens(
                        accessToken = refreshTokenInfo.accessToken,
                        refreshToken = tokenInfo.refreshToken!!
                    )
                }
            }
        }
    }

    suspend fun getSettings(): Settings {
        return tokenClient.get("$url$goTrue/settings")
    }

    /**
     * Generates a magic link for various cases (see [MagicLinkType]). This call requires the user to have the role admin.
     *
     * @param type The type of magic link to generate
     * @param email The email for which the magic link should be generated and to be sent to
     * @param password The password in case of a [MagicLinkType.signup]. Required only if signup.
     * @param data The data to provide additionally to the signup. Required only if signup
     * @param redirectTo Redirect URL to send the user to after an email action. Defaults to SITE_URL.
     */
    suspend fun generateLink(
        type: MagicLinkType,
        email: String,
        password: String?,
        data: JsonElement,
        redirectTo: String?
    ): MagicLinkData {
        TODO("Implement me")
    }

    /**
     * Register a new user with an email and password.
     *
     * @param email The email to register
     * @param password The password to set for the new account. If not set it needs to be provided on [verify]
     */
    suspend fun signUp(email: String, password: String): UserInfo {
        TODO("Not implemented yet")
    }

    suspend fun signIn(email: String, password: String) {
        authClient = authClient.config {
            developmentMode = true
            defaultRequest {
                header("apikey", apiKey)
                header(HttpHeaders.ContentType, "application/json")
            }
            expectSuccess = false
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
            installAuth(email, password)
        }
    }

    fun auth(): HttpClient = authClient

    /**
     *
     * @param type Can be signup, recover or invite
     * @param token The token provided with the link on creation
     * @param password The password to set in case of a signup. This is required if no password was set on [signUp].
     */
    suspend fun verify(type: MagicLinkType, token: String, password: String?): TokenInfo {
        TODO("Not implemented yet")
        // Can be GET or POST request
        // GET request receives a response as:
        // SITE_URL/#access_token=jwt-token-representing-the-user&token_type=bearer&expires_in=3600&refresh_token=a-refresh-token&type=invite
        // GET response with type invite or recovery should redirect user to password set
        // GET response with type signup can show welcome page
    }

    /**
     *
     * @param token Confirmation OTP delivered in SMS
     * @param redirectTo
     * @param phone Phone number sms TOP was delivered to
     */
    suspend fun verifyOTP(token: String, redirectTo: String?, phone: String) {
        TODO("Not implemented yet")
        // Same as verify, just with type "sms"
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

    suspend fun getUser(): UserInfo = authClient.get("$url$goTrue/user")
}
