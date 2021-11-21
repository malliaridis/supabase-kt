package io.supabase.realtime.helper

import kotlinx.coroutines.*

/**
 * Creates a timer that accepts a `timerCalc` function to perform calculated timeout retries, such as exponential backoff.
 *
 * @example
 *    let reconnectTimer = new Timer(() => this.connect(), function(tries){
 *      return [1000, 5000, 10000][tries - 1] || 10000
 *    })
 *    reconnectTimer.scheduleTimeout() // fires after 1000
 *    reconnectTimer.scheduleTimeout() // fires after 5000
 *    reconnectTimer.reset()
 *    reconnectTimer.scheduleTimeout() // fires after 1000
 */
open class Timer(
    val callback: suspend () -> Unit,
    val timerCalc: (tries: Int) -> Long,
    val job: Job? = null
) {
    var timer: Timer? = null
    var tries: Int = 0

    fun reset() {
        tries = 0

        clearTimeout(this)
    }

    // Cancels any previous scheduleTimeout and schedules callback
    fun scheduleTimeout() {
        timer?.let { clearTimeout(it) }

        timer = setTimeout(suspend {
            tries += 1
            callback()
        }, timerCalc(tries + 1))
    }
}

fun setInterval(call: suspend () -> Unit, interval: Long): Timer {
    val job = CoroutineScope(Dispatchers.Default).launch {
        while (true) {
            delay(interval)
            call()
        }
    }
    return Timer(call, { interval }, job)
}

fun clearInterval(timer: Timer) {
    timer.job?.cancel("Timer interval cleared.")
}

fun clearTimeout(timer: Timer) {
    timer.job?.cancel("Timeout cleared.")
}

fun setTimeout(callback: suspend () -> Unit, timeout: Long): Timer {
    val job = CoroutineScope(Dispatchers.Default).launch {
        delay(timeout)
        callback()
    }
    return Timer(callback, { timeout }, job)
}