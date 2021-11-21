package io.supabase.gotrue.helper

import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlin.math.round
import kotlin.random.Random

fun expiresAt(expiresIn: Long): Long {
    val timeNow = timeNow()
    return timeNow + expiresIn
}

fun uuid(): String {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(Regex("[xy]")) { c ->
        val r = (Random.nextInt(16)) or 0
        val v = if (c.value == "x") r else (r and 0x3) or 0x8
        v.toString(16)
    }
}

// TODO See if this is relevant
fun isBrowser() = false // typeof window !== 'undefined'

/**
 * TODO url default value is window?.location?.href ?: ""
 */
fun getParameterByName(name: String, url: String = ""): String? {
    val simplifiedName = name.replace(Regex("[\\[\\]]"), "\\$&")
    val regex = Regex("[?&#]$simplifiedName(=([^&#]*)|&|#|$)")
    val results = regex.matchEntire(url) ?: return null
    if (results.groups[2] == null) return ""
    return results.groups[2]?.value?.replace(Regex("\\+"), " ")?.decodeURLQueryComponent()
}

/**
 * Returns the current time in seconds.
 */
fun timeNow(): Long = round(Clock.System.now().toEpochMilliseconds().toDouble() / 1000).toLong()