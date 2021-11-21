package io.supabase.gotrue.main

import io.ktor.client.*
import io.supabase.gotrue.domain.UserInfo
import io.supabase.gotrue.ktor.GoTrueKtorClient
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val url = System.getenv("SUPABASE_URL")
        val apiKey = System.getenv("SUPABASE_API_KEY")

        val client = GoTrueKtorClient(url, apiKey, HttpClient())
        val result = client.signIn(System.getenv("SUPABASE_USERNAME"), System.getenv("SUPABASE_PASSWORD"))

        val userInfo: UserInfo = client.getUser()
        println("Hello, ${userInfo.email}!")
        println("Your data is ${userInfo.appMetadata} and your role is ${userInfo.role}.")
    }
}